package fr.insee.vtl.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VtlAssignmentTest {

    private ScriptEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ScriptEngineManager().getEngineByName("vtl");
    }

    @Test
    void testBooleans() throws ScriptException {
        ScriptContext context = engine.getContext();
        List<Boolean> a = List.of(false, false, true, true);
        List<Boolean> b = List.of(false, true, false, true);

        List<Boolean> and = List.of(false, false, false, true);
        List<Boolean> or = List.of(false, true, true, true);
        List<Boolean> xor = List.of(false, true, true, false);

        for (int i = 0; i < 4; i++) {
            context.setAttribute("a", a.get(i), ScriptContext.ENGINE_SCOPE);
            context.setAttribute("b", b.get(i), ScriptContext.ENGINE_SCOPE);

            engine.eval("" +
                    "andRes := a and b;" +
                    "orRes := a or b;" +
                    "xorRes := a xor b;"
            );
            assertThat(context.getAttribute("andRes")).isEqualTo(and.get(i));
            assertThat(context.getAttribute("orRes")).isEqualTo(or.get(i));
            assertThat(context.getAttribute("xorRes")).isEqualTo(xor.get(i));
        }

    }

    @Test
    void testVariableExpression() throws ScriptException {
        ScriptContext context = engine.getContext();
        context.setAttribute("foo", 123, ScriptContext.ENGINE_SCOPE);
        engine.eval("bar := foo");
        assertThat(context.getAttribute("bar"))
                .isSameAs(context.getAttribute("foo"));
    }

    @Test
    void testAssignment() throws ScriptException {
        Bindings bindings = engine.createBindings();
        engine.eval("a := 1234;", bindings);
        engine.eval("b := 1234.1234;", bindings);
        engine.eval("c := true;", bindings);
        engine.eval("d := false;", bindings);
        engine.eval("e := \"foo\";", bindings);
        engine.eval("f := null;", bindings);

        assertThat(bindings).containsAllEntriesOf(Map.of(
                "a", 1234L,
                "b", 1234.1234,
                "c", true,
                "d", false,
                "e", "foo"
        ));

        assertThat(bindings.get("f")).isNull();
    }
}