package gt.umg.pos.ui;

import gt.umg.pos.model.ItemVenta;
import gt.umg.pos.model.Venta;

import java.time.format.DateTimeFormatter;

public class TicketPrinter {

    private static final int    W    = 44;
    private static final String LINE = "─".repeat(W);
    private static final String DLINE= "═".repeat(W);

    public static void imprimir(Venta venta) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");

        System.out.println();
        System.out.println("┌" + DLINE + "┐");
        centrar("SISTEMA DE VENTAS POS");
        System.out.println("├" + LINE + "┤");
        centrar("FACTURA");
        System.out.println("│ No. " + pr(venta.getNumeroVenta(), W - 5) + "│");
        System.out.println("│ Fecha: " + pr(venta.getFecha().format(fmt), W - 8) + "│");
        System.out.println("├" + LINE + "┤");
        System.out.println("│ " + pr("PRODUCTO", 20) + pl("CANT", 4) + pl("SUBTOTAL", 18) + " │");
        System.out.println("├" + LINE + "┤");

        for (ItemVenta item : venta.getItems()) {
            String nombre   = truncar(item.getProducto().getNombre(), 20);
            String cantidad = String.valueOf(item.getCantidad());
            String subtotal = String.format("Q %,.2f", item.getSubtotal());
            System.out.println("│ " + pr(nombre, 20) + pl(cantidad, 4) + pl(subtotal, 18) + " │");
        }

        System.out.println("├" + LINE + "┤");
        System.out.println("│ " + pr("SUBTOTAL:",  25) + pl(String.format("Q %,.2f", venta.getSubtotal()), 17) + " │");
        System.out.println("│ " + pr("IVA (12%):", 25) + pl(String.format("Q %,.2f", venta.getIva()),      17) + " │");
        System.out.println("│" + DLINE + "│");
        System.out.println("│ " + pr("TOTAL A PAGAR:", 25) + pl(String.format("Q %,.2f", venta.getTotal()), 17) + " │");
        System.out.println("└" + DLINE + "┘");
        centrar("Gracias por su compra!");
        System.out.println();
    }

    public static void mostrarVentaActual(Venta venta) {
        if (venta.isEmpty()) { System.out.println("  (sin productos)"); return; }
        System.out.println("┌" + LINE + "┐");
        System.out.println("│ " + pr("VENTA EN CURSO", W - 2) + " │");
        System.out.println("├" + LINE + "┤");
        for (ItemVenta item : venta.getItems()) {
            System.out.println("│ " + item.toString() + " │");
        }
        System.out.println("├" + LINE + "┤");
        System.out.printf("│ %-24s Q %,14.2f │%n", "Total provisional:", venta.getTotal());
        System.out.println("└" + LINE + "┘");
    }

    private static void centrar(String texto) {
        int padding = (W - texto.length()) / 2;
        String linea = " ".repeat(Math.max(0, padding)) + texto;
        System.out.println("│" + pr(linea, W) + "│");
    }

    private static String pr(String s, int n) { return String.format("%-" + n + "s", s); }
    private static String pl(String s, int n) { return String.format("%" + n + "s", s); }
    private static String truncar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
