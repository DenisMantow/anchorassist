package com.anchorassist;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AnchorAssistScreen extends Screen {

protected AnchorAssistScreen() {  
    super(Text.literal("Anchor Assist Settings"));  
}  

@Override  
protected void init() {  

    int centerX = this.width / 2;  
    int centerY = this.height / 2;  

    int buttonWidth = 180;  
    int buttonHeight = 20;  
    int spacing = 28;  

    // =========================  
    // AUTO HIT  
    // =========================  
    this.addDrawableChild(ButtonWidget.builder(  
            getToggleText("Auto Hit", AnchorAssist.autoHitEnabled),  
            button -> {  
                AnchorAssist.autoHitEnabled = !AnchorAssist.autoHitEnabled;  
                button.setMessage(getToggleText("Auto Hit", AnchorAssist.autoHitEnabled));  
            })  
            .dimensions(centerX - buttonWidth / 2, centerY - spacing * 2, buttonWidth, buttonHeight)  
            .build()  
    );  

    // =========================  
    // AUTO ANCHOR  
    // =========================  
    this.addDrawableChild(ButtonWidget.builder(  
            getToggleText("Auto Anchor", AnchorAssist.autoAnchorEnabled),  
            button -> {  
                AnchorAssist.autoAnchorEnabled = !AnchorAssist.autoAnchorEnabled;  
                button.setMessage(getToggleText("Auto Anchor", AnchorAssist.autoAnchorEnabled));  
            })  
            .dimensions(centerX - buttonWidth / 2, centerY - spacing, buttonWidth, buttonHeight)  
            .build()  
    );  

    // =========================  
    // ANCHOR SAFE  
    // =========================  
    this.addDrawableChild(ButtonWidget.builder(  
            getToggleText("Anchor Safe", AnchorAssist.anchorSafeEnabled),  
            button -> {  
                AnchorAssist.anchorSafeEnabled = !AnchorAssist.anchorSafeEnabled;  
                button.setMessage(getToggleText("Anchor Safe", AnchorAssist.anchorSafeEnabled));  
            })  
            .dimensions(centerX - buttonWidth / 2, centerY, buttonWidth, buttonHeight)  
            .build()  
    );  

    // =========================  
    // FAST TOTEM  
    // =========================  
    this.addDrawableChild(ButtonWidget.builder(  
            getToggleText("Fast Totem", AnchorAssist.fastTotemEnabled),  
            button -> {  
                AnchorAssist.fastTotemEnabled = !AnchorAssist.fastTotemEnabled;  
                button.setMessage(getToggleText("Fast Totem", AnchorAssist.fastTotemEnabled));  
            })  
            .dimensions(centerX - buttonWidth / 2, centerY + spacing, buttonWidth, buttonHeight)  
            .build()  
    );  
}  

// =========================  
// TEXT STYLE (Modern ON/OFF)  
// =========================  
private Text getToggleText(String name, boolean enabled) {  
    return Text.literal(name + ": ")  
            .append(enabled  
                    ? Text.literal("ON").formatted(Formatting.GREEN)  
                    : Text.literal("OFF").formatted(Formatting.RED)  
            );  
}  

// =========================  
// TITLE + KEYBINDS INFO  
// =========================  
@Override  
public void render(DrawContext context, int mouseX, int mouseY, float delta) {  

    this.renderBackground(context);  
    super.render(context, mouseX, mouseY, delta);  

    int centerX = this.width / 2;  

    context.drawCenteredTextWithShadow(  
            this.textRenderer,  
            Text.literal("Anchor Assist").formatted(Formatting.AQUA, Formatting.BOLD),  
            centerX,  
            30,  
            0xFFFFFF  
    );  

    context.drawCenteredTextWithShadow(  
            this.textRenderer,  
            Text.literal("R = Hit | G = Anchor | Y = Safe | T = Totem | Shift = GUI")  
                    .formatted(Formatting.GRAY),  
            centerX,  
            45,  
            0xFFFFFF  
    );  
}  

@Override  
public boolean shouldPause() {  
    return false;  
}

}
