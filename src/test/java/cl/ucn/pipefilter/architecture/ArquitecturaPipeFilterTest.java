package cl.ucn.pipefilter.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import io.vertx.core.AbstractVerticle;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "cl.ucn.pipefilter", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquitecturaPipeFilterTest {

    @ArchTest
    public static final ArchRule filtros_deben_ser_verticles =
            classes()
                    .that().resideInAPackage("..verticles..")
                    .should().beAssignableTo(AbstractVerticle.class)
                    .because("cada filtro del pipeline debe ejecutarse como un verticle de Vert.x");

    @ArchTest
    public static final ArchRule filtros_no_deben_invocar_otros_filtros_directamente =
            noClasses()
                    .that().resideInAPackage("..verticles..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..verticles..")
                    .because("en Pipe & Filter los filtros deben comunicarse mediante pipes, en este caso canales del EventBus");

    @ArchTest
    public static final ArchRule modelo_no_debe_depender_de_capas_de_ejecucion =
            noClasses()
                    .that().resideInAPackage("..model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..verticles..", "..config..", "..main..", "io.vertx..")
                    .because("las entidades del dominio no deben conocer la ejecucion reactiva ni el arranque de la aplicacion");

    @ArchTest
    public static final ArchRule configuracion_no_debe_depender_del_pipeline =
            noClasses()
                    .that().resideInAPackage("..config..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..verticles..", "..main..")
                    .because("la configuracion debe ser reutilizable y no acoplarse al flujo concreto de filtros");
}
