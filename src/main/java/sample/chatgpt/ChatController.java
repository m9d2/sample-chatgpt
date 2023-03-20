package sample.chatgpt;

import cn.m9d2.chatgpt.MessageListener;
import cn.m9d2.chatgpt.model.chat.Completions;
import cn.m9d2.chatgpt.model.chat.CompletionsResponse;
import cn.m9d2.chatgpt.model.chat.Message;
import cn.m9d2.chatgpt.service.ChatService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    public ResponseEntity<CompletionsResponse> chat(@RequestParam String content) {
        Completions completions = Completions.builder()
                .user("user")
                .messages(Lists.newArrayList(Message.builder().role("user").content(content).build()))
                .build();
        CompletionsResponse response = chatService.completions(completions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam String content) {
        SseEmitterUTF8 sseEmitter = new SseEmitterUTF8(0L);
        Completions completions = Completions.builder()
                .user("user")
                .messages(Lists.newArrayList(Message.builder().role("user").content(content).build()))
                .build();
        chatService.completions(completions, new MessageListener() {
            @Override
            public void onMessaged(String data) {
                try {
                    sseEmitter.send(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onDone() {
                sseEmitter.complete();
            }
        });
        return sseEmitter;
    }
}
