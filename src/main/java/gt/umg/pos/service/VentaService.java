package gt.umg.pos.service;

import gt.umg.pos.db.SupabaseClient;
import gt.umg.pos.db.SupabaseConfig;
import gt.umg.pos.model.ItemVenta;
import gt.umg.pos.model.Producto;
import gt.umg.pos.model.Venta;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class VentaService {

    private ProductoService  productoService;
    private List<Venta>      historial = new ArrayList<>();
    private Venta            ventaActual;

    public VentaService(ProductoService productoService) {
        this.productoService = productoService;
        nuevaVenta();
    }

    public void nuevaVenta() {
        this.ventaActual = new Venta();
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public boolean agregarProductoAVenta(String codigo, int cantidad) {
        Optional<Producto> opt = productoService.buscarPorCodigo(codigo);
        if (opt.isEmpty()) {
            System.out.println("[!] Producto no encontrado: " + codigo);
            return false;
        }
        Producto p = opt.get();
        if (p.getStock() < cantidad) {
            System.out.printf("[!] Stock insuficiente. Disponible: %d%n", p.getStock());
            return false;
        }
        ventaActual.agregarItem(new ItemVenta(p, cantidad));
        return true;
    }

    public Optional<Venta> completarVenta() {
        if (ventaActual.isEmpty()) {
            System.out.println("[!] No hay productos en la venta.");
            return Optional.empty();
        }
        for (ItemVenta item : ventaActual.getItems()) {
            Producto p = item.getProducto();
            productoService.actualizarStock(p.getCodigo(), p.getStock() - item.getCantidad());
        }
        historial.add(ventaActual);
        if (SupabaseConfig.isConfigured()) {
            guardarVentaEnSupabase(ventaActual);
        }
        Venta completada = ventaActual;
        nuevaVenta();
        return Optional.of(completada);
    }

    public List<Venta> getHistorial() {
        return historial;
    }

    private void guardarVentaEnSupabase(Venta venta) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String jsonVenta = String.format(
            "{\"numero_venta\":\"%s\",\"fecha\":\"%s\",\"subtotal\":%.2f,\"iva\":%.2f,\"total\":%.2f,\"estado\":\"%s\"}",
            venta.getNumeroVenta(), venta.getFecha().format(fmt),
            venta.getSubtotal(), venta.getIva(), venta.getTotal(), venta.getEstado()
        );

        Optional<String> result = SupabaseClient.post(SupabaseConfig.ENDPOINT_VENTAS, jsonVenta);
        if (result.isEmpty()) {
            System.err.println("[DB] No se pudo guardar la venta.");
            return;
        }

        String idVenta = extraerValor(result.get(), "id");
        System.out.println("[DB] Venta guardada con ID: " + idVenta);

        for (ItemVenta item : venta.getItems()) {
            String jsonDetalle = String.format(
                "{\"venta_id\":%s,\"codigo_producto\":\"%s\",\"nombre_producto\":\"%s\",\"cantidad\":%d,\"precio_unitario\":%.2f,\"subtotal\":%.2f}",
                idVenta, item.getProducto().getCodigo(), item.getProducto().getNombre(),
                item.getCantidad(), item.getProducto().getPrecio(), item.getSubtotal()
            );
            SupabaseClient.post(SupabaseConfig.ENDPOINT_DETALLE, jsonDetalle);
        }
    }

    private String extraerValor(String json, String campo) {
        String patron = "\"" + campo + "\":";
        int inicio = json.indexOf(patron);
        if (inicio == -1) return "null";
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
