package gt.umg.pos.model;

public class ItemVenta {

    private Producto producto;
    private int      cantidad;
    private double   subtotal;

    public ItemVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = producto.getPrecio() * cantidad;
    }

    public Producto getProducto() { return producto; }
    public int      getCantidad() { return cantidad; }
    public double   getSubtotal() { return subtotal; }

    @Override
    public String toString() {
        return String.format("%-20s x%3d  Q%8.2f", producto.getNombre(), cantidad, subtotal);
    }
}
