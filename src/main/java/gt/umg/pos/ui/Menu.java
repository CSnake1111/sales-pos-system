package gt.umg.pos.ui;

import gt.umg.pos.model.Producto;
import gt.umg.pos.model.Venta;
import gt.umg.pos.service.ProductoService;
import gt.umg.pos.service.VentaService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {

    private final Scanner        sc = new Scanner(System.in);
    private final ProductoService productoService;
    private final VentaService    ventaService;

    public Menu(ProductoService productoService, VentaService ventaService) {
        this.productoService = productoService;
        this.ventaService    = ventaService;
    }

    public void iniciar() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║     SISTEMA POS                            ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        boolean salir = false;
        while (!salir) {
            System.out.println("══════════════════════════════════════════");
            System.out.println("  1. Agregar producto al catalogo");
            System.out.println("  2. Registrar venta");
            System.out.println("  3. Ver catalogo");
            System.out.println("  4. Ver historial de ventas");
            System.out.println("  0. Salir");
            System.out.println("══════════════════════════════════════════");

            switch (leerEntero("Opcion: ")) {
                case 1 -> menuAgregarProducto();
                case 2 -> menuRegistrarVenta();
                case 3 -> listarProductos();
                case 4 -> mostrarHistorial();
                case 0 -> salir = true;
                default -> System.out.println("[!] Opcion no valida.\n");
            }
        }
        System.out.println("\nHasta luego.\n");
    }

    private void menuAgregarProducto() {
        System.out.println("\n-- AGREGAR PRODUCTO --");
        System.out.print("  Codigo : "); String codigo = sc.nextLine().trim().toUpperCase();
        System.out.print("  Nombre : "); String nombre = sc.nextLine().trim();
        double precio = leerDecimal("  Precio: Q ");
        int    stock  = leerEntero("  Stock : ");
        Producto p = productoService.agregar(new Producto(codigo, nombre, precio, stock));
        System.out.println("  Producto agregado -> " + p + "\n");
    }

    private void menuRegistrarVenta() {
        System.out.println("\n-- REGISTRAR VENTA -- (escribe 'fin' para terminar)");

        while (true) {
            TicketPrinter.mostrarVentaActual(ventaService.getVentaActual());
            System.out.print("\n  Codigo (o 'fin'): ");
            String codigo = sc.nextLine().trim().toUpperCase();

            if (codigo.equals("FIN")) break;
            if (codigo.equals("CANCELAR")) {
                ventaService.nuevaVenta();
                System.out.println("  Venta cancelada.\n");
                return;
            }

            Optional<Producto> prod = productoService.buscarPorCodigo(codigo);
            if (prod.isEmpty()) { System.out.println("  [!] Producto no encontrado."); continue; }

            System.out.println("  -> " + prod.get());
            int cantidad = leerEntero("  Cantidad: ");
            if (cantidad <= 0) { System.out.println("  [!] Cantidad invalida."); continue; }
            if (ventaService.agregarProductoAVenta(codigo, cantidad)) System.out.println("  Agregado.");
        }

        if (ventaService.getVentaActual().isEmpty()) {
            System.out.println("  Venta vacia, regresando al menu.\n");
            return;
        }

        TicketPrinter.mostrarVentaActual(ventaService.getVentaActual());
        System.out.print("\n  Confirmar venta? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            ventaService.completarVenta().ifPresent(TicketPrinter::imprimir);
        } else {
            ventaService.nuevaVenta();
            System.out.println("  Venta cancelada.\n");
        }
    }

    private void listarProductos() {
        System.out.println("\n-- CATALOGO --");
        productoService.listarTodos().stream()
            .sorted((a, b) -> a.getCodigo().compareTo(b.getCodigo()))
            .forEach(p -> System.out.println("  " + p));
        System.out.println();
    }

    private void mostrarHistorial() {
        List<Venta> historial = ventaService.getHistorial();
        System.out.println("\n-- HISTORIAL DE VENTAS --");
        if (historial.isEmpty()) { System.out.println("  Sin ventas registradas.\n"); return; }
        for (Venta v : historial) {
            System.out.printf("  %s  ->  Q %,.2f  [%s]%n",
                v.getNumeroVenta(), v.getTotal(), v.getEstado());
        }
        System.out.println();
    }

    private int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  [!] Ingresa un numero entero."); }
        }
    }

    private double leerDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  [!] Ingresa un numero decimal (usa punto)."); }
        }
    }
}
