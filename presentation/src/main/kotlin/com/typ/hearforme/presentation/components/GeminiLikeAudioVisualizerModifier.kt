package com.typ.hearforme.presentation.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private const val SHADER_SRC = """
    uniform float2 resolution;
    uniform float time;
    uniform float rmsValue; // The audio amplitude (0.0 to 1.0)
    
    uniform float3 c1;
    uniform float3 c2;
    uniform float3 c3;
    uniform float3 c4;
    uniform float3 bgColor;
    
    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution.xy;
        
        // Slower, more fluid water distortion
        // Using layered sines for an organic 'wavy' feel
        float movementSpeed = time * 0.4;
        float distortion = sin(uv.y * 5.0 + movementSpeed) * 0.02 
                         + sin(uv.y * 12.0 - movementSpeed * 0.8) * 0.01;
        
        float2 warpedUv = uv;
        warpedUv.x += distortion;
        warpedUv.y += sin(uv.x * 4.0 + movementSpeed) * 0.015;
        
        // Define minimum amplitude floor
        float minAmp = 0.15;
        float audioBump = max(rmsValue, 0.0) * 0.25 + minAmp;
        
        // Blob placement with smooth oscillating horizontal motion
        // Blobs move left/right but stay within view
        
        // Blob 1
        float2 pos1 = float2(0.25 + sin(time * 0.3) * 0.15, 1.05);
        float2 scale1 = float2(1.2, 1.8 / audioBump); 
        float i1 = exp(-dot((warpedUv - pos1) * scale1, (warpedUv - pos1) * scale1));
        
        // Blob 2
        float2 pos2 = float2(0.75 + cos(time * 0.4) * 0.15, 1.05);
        float2 scale2 = float2(1.0, 1.5 / (audioBump + 0.05));
        float i2 = exp(-dot((warpedUv - pos2) * scale2, (warpedUv - pos2) * scale2));
        
        // Blob 3
        float2 pos3 = float2(0.5 + sin(time * 0.5) * 0.2, 1.05);
        float2 scale3 = float2(1.4, 2.2 / audioBump);
        float i3 = exp(-dot((warpedUv - pos3) * scale3, (warpedUv - pos3) * scale3));
        
        // Blob 4
        float2 pos4 = float2(0.15 + cos(time * 0.35) * 0.1, 1.05);
        float2 scale4 = float2(0.9, 1.6 / audioBump);
        float i4 = exp(-dot((warpedUv - pos4) * scale4, (warpedUv - pos4) * scale4));
        
        float totalIntensity = i1 + i2 + i3 + i4;
        float3 finalColor = bgColor;
        
        if (totalIntensity > 0.001) {
            float3 glow = (c1 * i1 + c2 * i2 + c3 * i3 + c4 * i4) / totalIntensity;
            
            // Softer falloff for the fluid look
            float alpha = clamp(totalIntensity * 1.3, 0.0, 1.0);
            alpha = smoothstep(0.0, 1.2, alpha);
            
            finalColor = mix(bgColor, glow, alpha);
        }
        
        return half4(finalColor, 1.0);
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun RuntimeShader.setColorUniformFloat3(uniformName: String, color: Color) {
    setFloatUniform(uniformName, color.red, color.green, color.blue)
}

@Stable
fun Modifier.geminiLikeAudioWaveBackground(
    rmsValue: Float,
    color1: Color = Color(0xFF1976D2), // Vivid Blue
    color2: Color = Color(0xFF9C27B0), // Vibrant Purple
    color3: Color = Color(0xFFE91E63), // Vibrant Pink
    color4: Color = Color(0xFF00BCD4), // Vivid Cyan
    backgroundColor: Color = Color(0xFFF5F7F9), // Light background
): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(SHADER_SRC) }
        val brush = remember(shader) { ShaderBrush(shader) }

        val transition = rememberInfiniteTransition(label = "ShaderTime")
        val time by transition.animateFloat(
            initialValue = 0f,
            targetValue = 628.318f,
            animationSpec = infiniteRepeatable(
                animation = tween(200_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "TimeAnimation"
        )

        Modifier.drawBehind {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("rmsValue", rmsValue)

            shader.setColorUniformFloat3("c1", color1)
            shader.setColorUniformFloat3("c2", color2)
            shader.setColorUniformFloat3("c3", color3)
            shader.setColorUniformFloat3("c4", color4)
            shader.setColorUniformFloat3("bgColor", backgroundColor)

            drawRect(brush = brush)
        }
    } else {
        this
    }
}
