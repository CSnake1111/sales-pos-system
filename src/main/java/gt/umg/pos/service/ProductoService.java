package gt.umg.pos.service;

import gt.umg.pos.db.SupabaseClient;
import gt.umg.pos.db.SupabaseConfig;
import gt.umg.pos.model.Producto;

import java.util.*;

public class ProductoService {

    private Map<String, Producto> catalogo = new HashMap<>();
    private int nextId = 1;

    public ProductoService() {
        cargarProductosDemo();
    }

    private void cargarProductosDemo() {
        agregar(new Producto("P001", "Agua pura 500ml",   3.00, 100));
        agregar(new Producto("P002", "Refresco 350ml",    7.00,  80));
        agregar(new Producto("P003", "Pan francés",       1.50, 200));
        agregar(new Producto("P004", "Leche 1L",         15.00,  50));
        agregar(new Producto("P005", "Jabón de baño",    12.00,  60));
        agregar(new Producto("P006", "Lapicero azul",     3.50, 150));
        agregar(new Producto("P007", "Cuaderno 80 hojas",18.00,  40));
        agregar(new Producto("P008", "Chicles",           2.00, 300));
    }

    public Producto agregar(Producto p) {
        p.setId(nextId++);
        catalogo.put(p.getCodigo(), p);
        if (SupabaseConfig.isConfigured()) {
            guardarEnSupabase(p);
        }
        return p;
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(catalogo.get(codigo.toUpperCase()));
    }

    public List<Producto> listarTodos() {
        return new ArrayList<>(catalogo.values());
    }

    public boolean actualizarStock(String codigo, int nuevaCantidad) {
        Producto p = catalogo.get(codigo);
        if (p == null) return false;
        p.setStock(nuevaCantidad);
        if (SupabaseConfig.isConfigured()) {
            SupabaseClient.patch(
                SupabaseConfig.ENDPOINT_PRODUCTOS + "?codigo=eq." + codigo,
                String.format("{\"stock\":%d}", nuevaCantidad)
            );
        }
        return true;
    }

    private void guardarEnSupabase(Producto p) {
        String json = String.format(
            "{\"codigo\":\"%s\",\"nombre\":\"%s\",\"precio\":%.2f,\"stock\":%d}",
            p.getCodigo(), p.getNombre(), p.getPrecio(), p.getStock()
        );
        Optional<String> result = SupabaseClient.upsert(SupabaseConfig.ENDPOINT_PRODUCTOS, json);
        if (result.isPresent()) System.out.println("[DB] Producto guardado: " + p.getCodigo());
    }

    public void cargarDesdeSupabase() {
        if (!SupabaseConfig.isConfigured()) return;

        Optional<String> response = SupabaseClient.get(SupabaseConfig.ENDPOINT_PRODUCTOS);
        response.ifPresent(json -> {
            catalogo.clear();
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]"))   json = json.substring(0, json.length() - 1);
            if (json.isBlank()) { System.out.println("[DB] Sin productos en BD."); return; }

            String[] registros = json.split("\\},\\s*\\{");
            for (String reg : registros) {
                try {
                    String codigo = extraerValor(reg, "codigo");
                    String nombre = extraerValor(reg, "nombre");
                    double precio = Double.parseDouble(extraerValor(reg, "precio"));
                    int    stock  = Integer.parseInt(extraerValor(reg, "stock"));
                    int    id     = Integer.parseInt(extraerValor(reg, "id"));
                    Producto p = new Producto(id, codigo, nombre, precio, stock);
                    catalogo.put(codigo, p);
                    if (id >= nextId) nextId = id + 1;
                } catch (Exception e) {
                    System.err.println("[DB] Error parseando registro: " + e.getMessage());
                }
            }
            System.out.println("[DB] " + catalogo.size() + " productos cargados desde Supabase.");
        });
    }

    private String extraerValor(String json, String campo) {
        String patron = "\"" + campo + "\":";
        int inicio = json.indexOf(patron);
        if (inicio == -1) return "0";
        inicio += patron.length();
        char primer = json.charAt(inicio);
        if (primer == '"') {
            int fin = json.indexOf('"', inicio + 1);
            return json.substring(inicio + 1, fin);
        } else {
            int fin = json.indexOf(',', inicio);
            if (fin == -1) fin = json.indexOf('}', inicio);
            return json.substring(inicio, fin).trim();
        }
    }
}
