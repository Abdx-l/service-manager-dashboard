package dev.akhras;
import org.springframework.boot.*;import org.springframework.boot.autoconfigure.*;import org.springframework.context.annotation.*;import org.springframework.scheduling.annotation.*;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.core.userdetails.*;import org.springframework.security.provisioning.InMemoryUserDetailsManager;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.web.socket.config.annotation.*;import org.springframework.messaging.simp.config.MessageBrokerRegistry;
@SpringBootApplication @EnableScheduling @EnableWebSocketMessageBroker
public class Application implements WebSocketMessageBrokerConfigurer{
 public static void main(String[]a){SpringApplication.run(Application.class,a);}
 public void registerStompEndpoints(StompEndpointRegistry r){r.addEndpoint("/ws");}
 public void configureMessageBroker(MessageBrokerRegistry r){r.enableSimpleBroker("/topic");}
 @Bean SecurityFilterChain security(HttpSecurity h)throws Exception{return h.csrf(c->c.disable()).authorizeHttpRequests(a->a.requestMatchers("/ws","/actuator/health").permitAll().anyRequest().authenticated()).httpBasic(b->{}).build();}
 @Bean UserDetailsService users(){return new InMemoryUserDetailsManager(User.withUsername("admin").password(new BCryptPasswordEncoder().encode(System.getenv().getOrDefault("ADMIN_PASSWORD","local-change-me"))).roles("ADMIN").build());}
}
