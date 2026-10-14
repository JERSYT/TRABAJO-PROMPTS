package com.ferreteria.inventario.config;

import com.ferreteria.inventario.catalog.entity.Marca;
import com.ferreteria.inventario.catalog.entity.Presentacion;
import com.ferreteria.inventario.catalog.entity.Producto;
import com.ferreteria.inventario.catalog.entity.Referencia;
import com.ferreteria.inventario.catalog.entity.UnidadMedida;
import com.ferreteria.inventario.catalog.repository.MarcaRepository;
import com.ferreteria.inventario.catalog.repository.PresentacionRepository;
import com.ferreteria.inventario.catalog.repository.ProductoRepository;
import com.ferreteria.inventario.catalog.repository.ReferenciaRepository;
import com.ferreteria.inventario.catalog.repository.UnidadMedidaRepository;
import com.ferreteria.inventario.customer.entity.Tercero;
import com.ferreteria.inventario.customer.repository.TerceroRepository;
import com.ferreteria.inventario.document.entity.ComprobanteConsecutivo;
import com.ferreteria.inventario.document.repository.ComprobanteConsecutivoRepository;
import com.ferreteria.inventario.inventory.entity.InventarioMovimiento;
import com.ferreteria.inventario.inventory.entity.InventarioSaldo;
import com.ferreteria.inventario.inventory.repository.InventarioMovimientoRepository;
import com.ferreteria.inventario.inventory.repository.InventarioSaldoRepository;
import com.ferreteria.inventario.user.entity.Rol;
import com.ferreteria.inventario.user.entity.Usuario;
import com.ferreteria.inventario.user.repository.RolRepository;
import com.ferreteria.inventario.user.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Componente de aprovisionamiento inicial seguro:
 * - Inicializa roles y el primer administrador si la base de datos está vacía.
 * - Asegura la inicialización de consecutivos y catálogos mínimos de demostración.
 */
@Component
public class AdminInitializerRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializerRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ComprobanteConsecutivoRepository consecutivoRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final PresentacionRepository presentacionRepository;
    private final ReferenciaRepository referenciaRepository;
    private final InventarioSaldoRepository saldoRepository;
    private final InventarioMovimientoRepository movimientoRepository;
    private final TerceroRepository terceroRepository;

    @Value("${app.admin.initial.username:admin_ferreteria}")
    private String initialAdminUsername;

    @Value("${app.admin.initial.password:Admin123*}")
    private String initialAdminPassword;

    public AdminInitializerRunner(UsuarioRepository usuarioRepository,
                                  RolRepository rolRepository,
                                  PasswordEncoder passwordEncoder,
                                  ComprobanteConsecutivoRepository consecutivoRepository,
                                  UnidadMedidaRepository unidadMedidaRepository,
                                  ProductoRepository productoRepository,
                                  MarcaRepository marcaRepository,
                                  PresentacionRepository presentacionRepository,
                                  ReferenciaRepository referenciaRepository,
                                  InventarioSaldoRepository saldoRepository,
                                  InventarioMovimientoRepository movimientoRepository,
                                  TerceroRepository terceroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.consecutivoRepository = consecutivoRepository;
        this.unidadMedidaRepository = unidadMedidaRepository;
        this.productoRepository = productoRepository;
        this.marcaRepository = marcaRepository;
        this.presentacionRepository = presentacionRepository;
        this.referenciaRepository = referenciaRepository;
        this.saldoRepository = saldoRepository;
        this.movimientoRepository = movimientoRepository;
        this.terceroRepository = terceroRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Roles obligatorios
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_ADMINISTRADOR", "Acceso total administrativo")));
        Rol rolOperativo = rolRepository.findByNombre("ROLE_USUARIO_OPERATIVO")
                .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_USUARIO_OPERATIVO", "Usuario operativo de mostrador")));

        // 2. Primer usuario Administrador (Asegurar siempre credenciales válidas)
        Usuario admin = usuarioRepository.findByUsername(initialAdminUsername)
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername(initialAdminUsername);
                    return u;
                });
        admin.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
        admin.setNombreCompleto("Administrador General de la Ferretería");
        admin.setEmail("admin@ferreteria.local");
        admin.setActivo(true);
        admin.setRoles(Set.of(rolAdmin));
        usuarioRepository.save(admin);
        log.info("Usuario Administrador configurado con éxito: {}", initialAdminUsername);

        // Usuario Operativo demostrativo (Asegurar siempre credenciales válidas)
        Usuario operador = usuarioRepository.findByUsername("operador_mostrador")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setUsername("operador_mostrador");
                    return u;
                });
        operador.setPasswordHash(passwordEncoder.encode("Operador123*"));
        operador.setNombreCompleto("Carlos Operativo de Ventas");
        operador.setEmail("operador@ferreteria.local");
        operador.setActivo(true);
        operador.setRoles(Set.of(rolOperativo));
        usuarioRepository.save(operador);
        log.info("Usuario Operativo configurado con éxito: operador_mostrador");

        // 3. Consecutivos iniciales (RN-12)
        consecutivoRepository.findByTipoDocumento("VENTA")
                .orElseGet(() -> consecutivoRepository.save(new ComprobanteConsecutivo("VENTA", "VTA-", 100L)));
        consecutivoRepository.findByTipoDocumento("COMPRA")
                .orElseGet(() -> consecutivoRepository.save(new ComprobanteConsecutivo("COMPRA", "CMP-", 50L)));
        consecutivoRepository.findByTipoDocumento("DEVOLUCION")
                .orElseGet(() -> consecutivoRepository.save(new ComprobanteConsecutivo("DEVOLUCION", "DEV-", 10L)));
        consecutivoRepository.findByTipoDocumento("AJUSTE")
                .orElseGet(() -> consecutivoRepository.save(new ComprobanteConsecutivo("AJUSTE", "AJU-", 5L)));

        // 4. Catálogos base de demostración si están vacíos
        if (unidadMedidaRepository.count() == 0) {
            UnidadMedida und = unidadMedidaRepository.save(new UnidadMedida(null, "UND", "Unidad"));
            UnidadMedida mtr = unidadMedidaRepository.save(new UnidadMedida(null, "MTR", "Metro"));
            UnidadMedida kgm = unidadMedidaRepository.save(new UnidadMedida(null, "KGM", "Kilogramo"));
            UnidadMedida gln = unidadMedidaRepository.save(new UnidadMedida(null, "GLN", "Galón"));
            UnidadMedida cja = unidadMedidaRepository.save(new UnidadMedida(null, "CJA", "Caja"));

            Marca mHerragro = marcaRepository.save(new Marca(null, "Herragro", true));
            Marca mStanley = marcaRepository.save(new Marca(null, "Stanley", true));
            Marca mPintuco = marcaRepository.save(new Marca(null, "Pintuco", true));

            Presentacion pUnidad = presentacionRepository.save(new Presentacion(null, "Unidad Suelta", true));
            Presentacion pCaja = presentacionRepository.save(new Presentacion(null, "Caja x 100", true));
            Presentacion pGalon = presentacionRepository.save(new Presentacion(null, "Galón 3.78L", true));

            Producto prodMartillo = productoRepository.save(new Producto(null, "Martillo de Uña 16oz", "Martillo forjado", true));
            Producto prodTornillo = productoRepository.save(new Producto(null, "Tornillo Drywall 1 1/4 pulg", "Tornillo fijación", true));
            Producto prodPintura = productoRepository.save(new Producto(null, "Pintura Vinilo Tipo 1 Blanco", "Pintura blanca lavable", true));

            Referencia r1 = new Referencia();
            r1.setProducto(prodMartillo);
            r1.setMarca(mHerragro);
            r1.setPresentacion(pUnidad);
            r1.setUnidadMedida(und);
            r1.setCodigoBarras("7701234567891");
            r1.setPrecioVenta(new BigDecimal("32000.00"));
            r1.setActivo(true);
            referenciaRepository.save(r1);

            Referencia r2 = new Referencia();
            r2.setProducto(prodTornillo);
            r2.setMarca(mStanley);
            r2.setPresentacion(pCaja);
            r2.setUnidadMedida(cja);
            r2.setCodigoBarras("7701234567892");
            r2.setPrecioVenta(new BigDecimal("15000.00"));
            r2.setActivo(true);
            referenciaRepository.save(r2);

            Referencia r3 = new Referencia();
            r3.setProducto(prodPintura);
            r3.setMarca(mPintuco);
            r3.setPresentacion(pGalon);
            r3.setUnidadMedida(gln);
            r3.setCodigoBarras("7701234567893");
            r3.setPrecioVenta(new BigDecimal("78000.00"));
            r3.setActivo(true);
            referenciaRepository.save(r3);

            // Saldos iniciales
            saldoRepository.save(new InventarioSaldo(r1, new BigDecimal("20.000"), new BigDecimal("22000.0000")));
            saldoRepository.save(new InventarioSaldo(r2, new BigDecimal("50.000"), new BigDecimal("9500.0000")));
            saldoRepository.save(new InventarioSaldo(r3, new BigDecimal("15.000"), new BigDecimal("56000.0000")));

            // Terceros de prueba
            Tercero prov = new Tercero();
            prov.setTipoDocumento("NIT");
            prov.setNumeroDocumento("900543210-1");
            prov.setRazonSocial("Distribuidora Ferretera Nacional S.A.S.");
            prov.setTipoPersona("JURIDICA");
            prov.setTelefono("6013204500");
            prov.setEmail("ventas@ferrenacional.local");
            prov.setDireccion("Zona Industrial Bogotá");
            prov.setEsCliente(false);
            prov.setEsProveedor(true);
            prov.setActivo(true);
            terceroRepository.save(prov);

            Tercero cli = new Tercero();
            cli.setTipoDocumento("CC");
            cli.setNumeroDocumento("1020304050");
            cli.setRazonSocial("Juan Perez Constructor");
            cli.setTipoPersona("NATURAL");
            cli.setTelefono("3109876543");
            cli.setEmail("juan.perez@email.local");
            cli.setDireccion("Carrera 15 # 45-20");
            cli.setEsCliente(true);
            cli.setEsProveedor(false);
            cli.setActivo(true);
            terceroRepository.save(cli);

            Tercero dual = new Tercero();
            dual.setTipoDocumento("NIT");
            dual.setNumeroDocumento("901234567-8");
            dual.setRazonSocial("Construcciones y Remodelaciones del Norte");
            dual.setTipoPersona("JURIDICA");
            dual.setTelefono("6017890123");
            dual.setEmail("admin@constructora.local");
            dual.setDireccion("Av. Boyacá # 120-15");
            dual.setEsCliente(true);
            dual.setEsProveedor(true); // Ambos roles (RN-10)
            dual.setActivo(true);
            terceroRepository.save(dual);

            log.info("Datos de demostración y catálogo de arranque cargados con éxito.");
        }
    }
}
