package gt.umg.pos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private static final double IVA_RATE = 0.12;

    private int            id;
    private String         numeroVenta;
    private LocalDateTime  fecha;
    private List<ItemVenta> items;
    private double         subtotal;
    private double         iva;
    private double         total;
    private String         estado;

    public Venta() {
        this.items  = new ArrayList<>();
        this.fecha  = LocalDateTime.now();
        this.estado = "COMPLETADA";
        this.numeroVenta = "VTA-" + fecha.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public void agregarItem(ItemVenta item) {
        for (ItemVenta e : items) {
            if (e.getProducto().getCodigo().equals(item.getProducto().getCodigo())) {
                items.remove(e);
                items.add(new ItemVenta(item.getProducto(), e.getCantidad() + item.getCantidad()));
                calcularTotales();
                return;
            }
        }
        items.add(item);
        calcularTotales();
    }

    private void calcularTotales() {
        this.subtotal = items.stream().mapToDouble(ItemVenta::getSubtotal).sum();
        this.iva      = this.subtotal * IVA_RATE;
        this.total    = this.subtotal + this.iva;
    }

    public int            getId()          { return id; }
    public void           setId(int id)    { this.id = id; }
    public String         getNumeroVenta() { return numeroVenta; }
    public LocalDateTime  getFecha()       { return fecha; }
    public List<ItemVenta> getItems()      { return items; }
    public double         getSubtotal()    { return subtotal; }
    public double         getIva()         { return iva; }
    public double         getTotal()       { return total; }
    public String         getEstado()      { return estado; }
    public void           setEstado(String e) { this.estado = e; }
    public boolean        isEmpty()        { return items.isEmpty(); }
}
