package com.takumistudios.morphmod.animation;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.*;
import com.geckolib.loading.math.value.Variable;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import java.util.*;
import java.util.regex.Pattern;

/** Isolated query namespace: importing a profile never replaces GeckoLib actor variables. */
public final class AnimationMath {
    public static final List<String> QUERIES = List.of("anim_time", "ground_speed", "pitch", "yaw", "right_hand_swing", "left_hand_swing", "is_sneaking", "is_sprinting", "is_swimming", "is_on_ground", "life_time", "head_x_rotation", "head_y_rotation", "limb_swing", "limb_swing_amount", "is_first_person");
    private static final Pattern SYMBOL = Pattern.compile("[a-z_][a-z0-9_]*(?:\\.[a-z_][a-z0-9_]*)+");
    private static final Map<String, Variable> VARIABLES = new HashMap<>();
    static {
        for (String query : QUERIES) {
            Variable variable = MathParser.getVariableFor("variable.morphmod_animation_" + query);
            variable.set(state -> state.getQueryValue(variable)); VARIABLES.put(query, variable);
        }
    }
    public static MathValue compile(String expression) {
        if (expression.length() > 2048 || expression.contains(";") || Pattern.compile("(?<![!<>=])=(?!=)").matcher(expression).find())
            throw new IllegalArgumentException("Expression exceeds limits or contains statements");
        String lower = expression.toLowerCase(Locale.ROOT);
        var matcher = SYMBOL.matcher(lower); StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String symbol = matcher.group(), replacement = symbol;
            if (symbol.startsWith("query.") || symbol.startsWith("q.")) {
                String query = symbol.substring(symbol.indexOf('.') + 1);
                if (!VARIABLES.containsKey(query)) throw new IllegalArgumentException("Unsupported MoLang query: " + symbol);
                replacement = "variable.morphmod_animation_" + query;
            } else if (!symbol.startsWith("math.")) throw new IllegalArgumentException("Unsupported MoLang variable: " + symbol);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return MathParser.create().compileMolang(result.toString());
    }
    public static final class Context {
        private final Reference2DoubleOpenHashMap<Variable> values = new Reference2DoubleOpenHashMap<>();
        private final ControllerState state = new ControllerState(null, null, 0, 0, false, null, null, values);
        public Context set(String name, double value) {
            Variable variable = VARIABLES.get(name);
            if (variable == null || !Double.isFinite(value)) throw new IllegalArgumentException("Invalid animation query " + name);
            values.put(variable, value); return this;
        }
        public double evaluate(MathValue value) {
            double result = value.get(state);
            if (!Double.isFinite(result)) throw new IllegalArgumentException("Non-finite animation result");
            return result;
        }
    }
    private AnimationMath() { }
}
