precision mediump float;
uniform vec4 u_Color;
uniform float u_DisableLight;

varying float v_attenuation;
 
void main() {
	gl_FragColor = vec4(vec3(u_Color)*max(v_attenuation,u_DisableLight),u_Color.a);
}