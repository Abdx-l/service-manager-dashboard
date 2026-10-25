package dev.akhras;
import org.springframework.web.bind.annotation.*;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.messaging.simp.SimpMessagingTemplate;import com.github.dockerjava.api.*;import com.github.dockerjava.api.model.*;import com.github.dockerjava.core.*;import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;import java.util.*;import java.net.*;import java.net.http.*;import java.time.*;
@RestController @RequestMapping("/services")
public class Services{
 final DockerClient docker;final SimpMessagingTemplate messages;final Set<String> allowed;final Map<String,String>health=new java.util.concurrent.ConcurrentHashMap<>();
 Services(SimpMessagingTemplate m){messages=m;allowed=new HashSet<>(Arrays.asList(System.getenv().getOrDefault("SERVICE_CONTAINERS","demo-service").split(",")));var config=DefaultDockerClientConfig.createDefaultConfigBuilder().build();docker=DockerClientImpl.getInstance(config,new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).build());}
 void permit(String name){if(!allowed.contains(name))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);}
 @GetMapping public List<Map<String,Object>>list(){var result=new ArrayList<Map<String,Object>>();for(String name:allowed){try{var i=docker.inspectContainerCmd(name).exec();result.add(Map.of("name",name,"state",i.getState().getStatus(),"started",String.valueOf(i.getState().getStartedAt()),"health",health.getOrDefault(name,"UNKNOWN")));}catch(Exception e){result.add(Map.of("name",name,"state","FAILED","health","UNKNOWN"));}}return result;}
 @PostMapping("/{name}/{operation}")public Map<String,String>control(@PathVariable String name,@PathVariable String operation){permit(name);switch(operation){case "start"->docker.startContainerCmd(name).exec();case "stop"->docker.stopContainerCmd(name).withTimeout(10).exec();case "restart"->docker.restartContainerCmd(name).withTimeout(10).exec();default->throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);}return Map.of("status","accepted");}
 @GetMapping("/{name}/logs")public List<String>logs(@PathVariable String name)throws Exception{permit(name);var lines=Collections.synchronizedList(new ArrayList<String>());try(var callback=new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>(){@Override public void onNext(Frame frame){lines.add(new String(frame.getPayload(),java.nio.charset.StandardCharsets.UTF_8));}}){docker.logContainerCmd(name).withStdOut(true).withStdErr(true).withTail(50).withFollowStream(false).exec(callback).awaitCompletion(3,java.util.concurrent.TimeUnit.SECONDS);}return lines;}
 @Scheduled(fixedDelay=5000)void poll(){
  String[]urls=System.getenv().getOrDefault("HEALTH_URLS","").split(",");
  for(String pair:urls){int i=pair.indexOf('=');if(i<1)continue;String name=pair.substring(0,i);if(!allowed.contains(name))continue;
   try{var response=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build().send(HttpRequest.newBuilder(URI.create(pair.substring(i+1))).timeout(Duration.ofSeconds(2)).GET().build(),HttpResponse.BodyHandlers.ofString());health.put(name,response.statusCode()==200?"UP":"DOWN");}catch(Exception e){health.put(name,"DOWN");}
  }
  messages.convertAndSend("/topic/services",list());
 }
}
