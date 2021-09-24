package com.mystic.holographicrenders.client;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;


public class TextboxScreenRoot extends LightweightGuiDescription {

    private boolean methodHasRan;
    public static String text;
    public static URL url;
    public final CompoundTag compoundTag = new CompoundTag();
    public static String urlText;
    public static int texture;

    public URL getURL(){
        return url;
    }

    public int getTexture(){
        return texture;
    }

    public TextboxScreenRoot() {
        createTextboxAndSaveURLs();
    }

    public void createTextboxAndSaveURLs()
    {
        WGridPanel root = new WGridPanel();
        WButton button = new WButton();
        WTextField textFieldWidget = new WTextField(Text.of("Please enter a valid URL!"));
        WLabel label = new WLabel("Save PNG or JPEG URL");
            if(!methodHasRan) {
                setRootPanel(root);
                root.setSize(256, 240);
                textFieldWidget.setMaxLength(90);
                methodHasRan = true;
                root.add(textFieldWidget, 0, 5, 15, 10);
                root.add(button, 4, 8, 6, 12);
                root.add(label, 4 ,0);
                root.validate(this);
            }
            button.setOnClick(() -> {
                try {
                    text = textFieldWidget.getText();
                    if(!Objects.equals(text, "")) {
                    textFieldWidget.setText("URL found and saved");
                    }
                   url = new URL(text);
                   urlText =  url.toString();
                   RenderDataProvider.TextureProvider.of(url.toString()).createFileAndLoad();
                   texture =  RenderDataProvider.TextureProvider.of(url.toString()).loadTexture("hologramimages/hologramimages/" + url.getFile().toLowerCase(Locale.ROOT) + ".png");
                } catch (MalformedURLException e) {
                    if(!e.toString().isEmpty()) {
                        textFieldWidget.setText("valid URL not found pls try again!");
                    }
                    methodHasRan = true;
                    root.validate(this);
                }
            });

    }
}