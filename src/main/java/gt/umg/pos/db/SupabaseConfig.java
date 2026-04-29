package gt.umg.pos.db;

public class SupabaseConfig {

    public static final String SUPABASE_URL     = System.getenv("SUPABASE_URL");
    public static final String SUPABASE_API_KEY = System.getenv("SUPABASE_API_KEY");

    public static final String ENDPOINT_PRODUCTOS = SUPABASE_URL + "/rest/v1/productos";
    public static final String ENDPOINT_VENTAS    = SUPABASE_URL + "/rest/v1/ventas";
    public static final String ENDPOINT_DETALLE   = SUPABASE_URL + "/rest/v1/detalle_ventas";

    public static boolean isConfigured() {
        return SUPABASE_URL != null && !SUPABASE_URL.isBlank() &&
                SUPABASE_API_KEY != null && !SUPABASE_API_KEY.isBlank();
    }
}