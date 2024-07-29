package io.kubemq.sdk.cq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponseMessage {

    private CommandMessageReceived commandReceived;
    private String clientId;
    private String requestId;
    private boolean isExecuted;
    private LocalDateTime timestamp;
    private String error;

    public CommandResponseMessage(CommandMessageReceived commandReceived, boolean isExecuted, String error, LocalDateTime timestamp) {
        this.commandReceived = commandReceived;
        this.clientId = "";
        this.requestId = "";
        this.isExecuted = isExecuted;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
        this.error = error;
    }

    public CommandResponseMessage validate() {
        if (commandReceived == null) {
            throw new IllegalArgumentException("Command response must have a command request.");
        } else if (commandReceived.getReplyChannel() == null || commandReceived.getReplyChannel().isEmpty()) {
            throw new IllegalArgumentException("Command response must have a reply channel.");
        }
        return this;
    }

    public CommandResponseMessage decode(kubemq.Kubemq.Response pbResponse) {
        this.clientId = pbResponse.getClientID();
        this.requestId = pbResponse.getRequestID();
        this.isExecuted = pbResponse.getExecuted();
        this.error = pbResponse.getError();
        this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(pbResponse.getTimestamp() / 1_000_000_000L), ZoneOffset.UTC);
        return this;
    }

    public kubemq.Kubemq.Response encode(String clientId) {
        return  kubemq.Kubemq.Response.newBuilder()
                .setClientID(clientId)
                .setRequestID(this.commandReceived.getId())
                .setReplyChannel(this.commandReceived.getReplyChannel())
                .setExecuted(this.isExecuted)
                .setError(this.error)
                .setTimestamp(this.timestamp.toEpochSecond(ZoneOffset.UTC) * 1_000_000_000L)
                .build();
    }

    @Override
    public String toString() {
        return "CommandResponseMessage: clientId=" + clientId + ", requestId=" + requestId + ", isExecuted=" + isExecuted + ", error=" + error + ", timestamp=" + timestamp;
    }
}
