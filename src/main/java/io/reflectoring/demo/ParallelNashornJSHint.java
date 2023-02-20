package io.reflectoring.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Component
public final class ParallelNashornJSHint {
    @Value("classpath:/META-INF/resources/webjars/jshint/2.13.1/jshint.js")
    Resource resourceFile;

    private static final String OPTIONS = "{es3: false," +
            "boss: true," +
            "browser: true," +
            "curly: true," +
            "eqnull: true," +
            "evil: true," +
            "immed: true," +
            "jquery: true," +
            "loopfunc: true," +
            "multistr: true," +
            "newcap: true," +
            "noarg: true," +
            "noempty: true," +
            "onecase: true," +
            "sub: true," +
            "trailing: true}";
    private static final AtomicLong COMPLETION_COUNT = new AtomicLong();

    public void start() throws IOException, ScriptException, NoSuchMethodException {
        final CompiledScript script = getScriptLines();
        script.eval();

        Invocable invocable = (Invocable) script.getEngine();
        String js = "(function goo() {" +
                "var hz = 1; console.log(hz);" +
                "})();";

        Object funcResult = invocable.invokeFunction("JSHINT", js, script.getEngine().eval("options = " + OPTIONS));
        System.out.println(funcResult);
        Map<String, Object> result = (Map<String, Object>) script.getEngine().eval("JSHINT.data()");

        String json = new ObjectMapper().writeValueAsString(result);

        System.out.println(json);
    }

    private CompiledScript getScriptLines() throws IOException {
        final URL resource = resourceFile.getURL();
        try (final InputStream stream = resource.openStream();
             final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final BufferedReader buffered = new BufferedReader(reader)) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            CompiledScript script;
            try {
                script = ((Compilable) engine).compile(buffered);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            return script;
        }
    }
}
