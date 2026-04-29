package gt.umg.pos;

import gt.umg.pos.db.SupabaseConfig;
import gt.umg.pos.service.ProductoService;
import gt.umg.pos.service.VentaService;
import gt.umg.pos.ui.Menu;

public class Main {

    public static void main(String[] args) {
        if (SupabaseConfig.isConfigured()) {
            System.out.println("[DB] Conectando a Supabase...");
        } else {
            System.out.println("[INFO] Modo local - los datos no se guardaran en BD.");
        }

        ProductoService productoService = new ProductoService();
        VentaService    ventaService    = new VentaService(productoService);

        if (SupabaseConfig.isConfigured()) {
            productoService.cargarDesdeSupabase();
        }

        new Menu(productoService, ventaService).iniciar();
    }
}
