package com.takumistudios.morphmod.animation;

import com.google.gson.*;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.MathParser;
import java.util.*;

/** Bounded vector timeline; MoLang evaluation is delegated to GeckoLib. */
public record AnimationChannel(List<Key> keys, int lookQueries) {
    public record Key(double time, MathValue[] pre, MathValue[] post, boolean catmull) { }
    public static AnimationChannel parse(JsonElement json) {
        List<Key> keys = new ArrayList<>();
        if (!json.isJsonObject()) keys.add(new Key(0, vector(json), vector(json), false));
        else if (json.getAsJsonObject().has("vector")) {
            MathValue[] vector = vector(json.getAsJsonObject().get("vector")); keys.add(new Key(0, vector, vector, false));
        } else {
            if (json.getAsJsonObject().size() > 4096) throw new IllegalArgumentException("Too many keyframes");
            for (var entry : json.getAsJsonObject().entrySet()) {
                double time = Double.parseDouble(entry.getKey());
                if (!Double.isFinite(time) || time < 0 || time > 3600) throw new IllegalArgumentException("Invalid keyframe time");
                JsonElement value = entry.getValue(); boolean catmull = false; MathValue[] pre, post;
                if (value.isJsonObject()) {
                    JsonObject frame = value.getAsJsonObject();
                    String mode = frame.has("lerp_mode") ? frame.get("lerp_mode").getAsString() : "linear";
                    if (!mode.equals("linear") && !mode.equals("catmullrom")) throw new IllegalArgumentException("Unsupported interpolation " + mode);
                    catmull = mode.equals("catmullrom");
                    if (frame.has("vector")) pre = post = vector(frame.get("vector"));
                    else { pre = vector(frame.has("pre") ? frame.get("pre") : frame.get("post")); post = vector(frame.has("post") ? frame.get("post") : frame.get("pre")); }
                } else pre = post = vector(value);
                keys.add(new Key(time, pre, post, catmull));
            }
        }
        if (keys.isEmpty()) throw new IllegalArgumentException("Empty animation channel");
        keys.sort(Comparator.comparingDouble(Key::time));
        for (int i = 1; i < keys.size(); i++) if (keys.get(i).time == keys.get(i - 1).time) throw new IllegalArgumentException("Duplicate keyframe time");
        int look = 0;
        for (Key key : keys) for (int axis = 0; axis < 3; axis++) {
            for (MathValue expression : new MathValue[]{key.pre[axis], key.post[axis]}) for (var variable : expression.getUsedVariables()) {
                if (variable.name().endsWith("_pitch") || variable.name().endsWith("_head_x_rotation")) look |= 1;
                if (variable.name().endsWith("_yaw") || variable.name().endsWith("_head_y_rotation")) look |= 2;
            }
        }
        return new AnimationChannel(List.copyOf(keys), look);
    }
    private static MathValue[] vector(JsonElement json) {
        if (json == null) throw new IllegalArgumentException("Missing keyframe vector");
        if (json.isJsonArray() && json.getAsJsonArray().size() != 3) throw new IllegalArgumentException("Expected three components");
        MathValue[] result = new MathValue[3];
        for (int i = 0; i < 3; i++) {
            JsonElement value = json.isJsonArray() ? json.getAsJsonArray().get(i) : json;
            if (!value.isJsonPrimitive() || value.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("Expected number or MoLang expression");
            if (value.getAsJsonPrimitive().isNumber()) {
                double number = value.getAsDouble(); if (!Double.isFinite(number)) throw new IllegalArgumentException("Non-finite animation constant");
                result[i] = MathParser.create().compileConstant(number);
            } else result[i] = AnimationMath.compile(value.getAsString());
        }
        return result;
    }
    public float sample(double time, int axis, AnimationMath.Context context) {
        if (time < keys.getFirst().time) return (float)context.evaluate(keys.getFirst().pre[axis]);
        int low = 0, high = keys.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (keys.get(mid).time <= time) low = mid + 1; else high = mid - 1;
        }
        int index = Math.max(0, high);
        Key a = keys.get(index);
        if (index + 1 == keys.size()) return (float)context.evaluate(a.post[axis]);
        Key b = keys.get(index + 1); double t = (time - a.time) / (b.time - a.time);
        double from = context.evaluate(a.post[axis]), to = context.evaluate(b.pre[axis]);
        if (!b.catmull) return (float)(from + (to - from) * t);
        double before = context.evaluate(keys.get(Math.max(0, index - 1)).post[axis]);
        double after = context.evaluate(keys.get(Math.min(keys.size() - 1, index + 2)).pre[axis]);
        return (float)(0.5 * ((2 * from) + (-before + to) * t + (2 * before - 5 * from + 4 * to - after) * t * t + (-before + 3 * from - 3 * to + after) * t * t * t));
    }
}
