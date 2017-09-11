package com.xxxtai.view;



import javax.swing.*;
import java.awt.*;

class OptionView extends JDialog {
    public static enum Option{SHIPMENT, UNLOADING, NUll};
    private static final long serialVersionUID = 1L;
    private OptionViewListener listener;

    OptionView(String optionName) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width / 6, screenSize.height / 4);

        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JLabel label = new JLabel(optionName);
        label.setFont(new Font("宋体", Font.BOLD, 30));

        RoundButton shipmentBtn = new RoundButton("装  货");
        RoundButton unloadingBtn = new RoundButton("卸  货");
        RoundButton cancelBtn = new RoundButton("取 消");

        shipmentBtn.addActionListener(e -> listener.setOptionName(Option.SHIPMENT));
        unloadingBtn.addActionListener(e -> listener.setOptionName(Option.UNLOADING));
        cancelBtn.addActionListener(e -> listener.setOptionName(Option.NUll));

        mainPanel.add(shipmentBtn);
        mainPanel.add(unloadingBtn);
        mainPanel.add(cancelBtn);
        this.getContentPane().add(mainPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    void setOnDialogListener(OptionViewListener listener) {
        this.listener = listener;
    }
}
