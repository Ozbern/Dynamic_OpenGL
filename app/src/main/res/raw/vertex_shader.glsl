attribute vec4 a_Position;
uniform mat4 u_Matrix;
uniform mat4 u_ModelMatrix;

varying float v_attenuation;

void main() {
	vec3 lightPos=vec3(1.0,1.0,1.0);
	vec3 vertexNormal=vec3(u_ModelMatrix*a_Position);
	float distanceToLight = length (lightPos-vertexNormal);
	vec3 vectorToLight = normalize(lightPos-vertexNormal);
	float attenuation = max(dot(vertexNormal,vectorToLight),0.0);
	attenuation=attenuation* (1.0 / (1.0+0.55 * distanceToLight));

	lightPos=vec3(-1.0,1.0,1.0);
	distanceToLight = length (lightPos-vertexNormal);
	vectorToLight = normalize(lightPos-vertexNormal);
	float attenuation2 = max(dot(vertexNormal,vectorToLight),0.0);
	attenuation2=attenuation2 * (1.0 / (1.0+0.15 * distanceToLight));
	
	v_attenuation=attenuation+attenuation2+0.45;


    gl_Position = u_Matrix * u_ModelMatrix * a_Position;
}