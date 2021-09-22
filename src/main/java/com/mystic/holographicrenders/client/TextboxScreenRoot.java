package com.mystic.holographicrenders.client;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TextboxScreenRoot extends LightweightGuiDescription {

    public String url; //my mod
    private String text; //my mod
    private WTextField textFieldWidget = new WTextField(Text.of("suggestion")); //other mod
    private WGridPanel root = new WGridPanel(); //other mod

    public TextboxScreenRoot() { //other mod
        setRootPanel(root);
        root.setSize(158, 150);
        setTextFieldWidget(); //my mod
        root.validate(this);
    }

    private void setTextFieldWidget(){ //other mod
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        int y = textFieldWidget.getY();
        int x = textFieldWidget.getX();
        fireThisThing(); // my mod
        //fire a line here from fireThisThing method
        textFieldWidget.setText(text);
        root.add(textFieldWidget, x, (y - 0), 15, 10);
    }

    public String getUrl() { // my mod
        return url;
    }

    public void fireThisThing(){ //my mod
        int y = textFieldWidget.getY(); //other mod
        int x = textFieldWidget.getX(); //other mod
        this.url = textFieldWidget.getText(); //other mod
        if (getUrl().contains("http") && getUrl().contains("jpeg") || getUrl().contains("http") && getUrl().contains("png")) {
            this.text = "URL saved";
        } else {
            this.text =  "no URL found please try again";
        }

    }
}