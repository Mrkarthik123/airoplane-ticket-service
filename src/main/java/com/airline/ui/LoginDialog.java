package com.airline.ui;

import com.airline.model.User;
import com.airline.service.AuthService;
import javax.swing.*;
import java.awt.*;

/** Login / register dialog shown before the main window. */
public class LoginDialog extends JDialog {
    private final AuthService auth;
    private User user;
    private String token;
    private boolean registerMode = false;

    private final JTextField nameField = Theme.field(20);
    private final JTextField emailField = Theme.field(20);
    private final JPasswordField passField = Theme.passwordField(20);
    private final JButton tabLogin;
    private final JButton tabRegister;
    private final JLabel title;
    private final JLabel error;
    private final JButton submit;

    public LoginDialog(Window owner, AuthService auth) {
        super(owner, "Sign in — Airoplane Ticket Service", ModalityType.APPLICATION_MODAL);
        this.auth = auth;
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(Theme.BG);
        setSize(430, 420);
        setLocationRelativeTo(owner);

        // sky strip on top
        SkyPanel sky = new SkyPanel();
        sky.setPreferredSize(new Dimension(430, 120));
        add(sky, BorderLayout.NORTH);

        JPanel card = Theme.card(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        title = Theme.title("Login", 19);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(title, g);

        JPanel tabs = new JPanel(new GridLayout(1, 2, 6, 0));
        tabs.setOpaque(false);
        tabLogin = Theme.button("Login");
        tabRegister = Theme.button("Register");
        tabLogin.addActionListener(e -> setMode(false));
        tabRegister.addActionListener(e -> setMode(true));
        tabs.add(tabLogin);
        tabs.add(tabRegister);
        g.gridy = 1; g.gridwidth = 2;
        card.add(tabs, g);

        nameField.setVisible(false);
        g.gridy = 2; g.gridwidth = 1; g.weightx = 0.3;
        card.add(Theme.label("Full name"), g);
        g.gridx = 1; g.weightx = 0.7;
        card.add(nameField, g);

        g.gridx = 0; g.gridy = 3; g.weightx = 0.3;
        card.add(Theme.label("Email"), g);
        g.gridx = 1; g.weightx = 0.7;
        card.add(emailField, g);

        g.gridx = 0; g.gridy = 4; g.weightx = 0.3;
        card.add(Theme.label("Password"), g);
        g.gridx = 1; g.weightx = 0.7;
        card.add(passField, g);

        error = Theme.muted("", 12);
        error.setForeground(Theme.BAD);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2; g.weightx = 0;
        card.add(error, g);

        g.gridy = 6; g.gridwidth = 2;
        submit = Theme.primary("Continue");
        submit.addActionListener(e -> submit());
        card.add(submit, g);

        add(card, BorderLayout.CENTER);

        JPanel foot = new JPanel(new GridLayout(1, 1));
        foot.setOpaque(false);
        foot.setBorder(Theme.pad(0, 20, 12, 20));
        foot.add(Theme.muted("Demo customer: demo@skyjet.in / demo123   ·   Admin: admin@skyjet.in / admin123", 11));
        add(foot, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(submit);
        setMode(false);
    }

    private void setMode(boolean reg) {
        registerMode = reg;
        title.setText(reg ? "Create account" : "Login");
        nameField.setVisible(reg);
        tabLogin.setBackground(reg ? Theme.CARD : Theme.NAVY);
        tabLogin.setForeground(reg ? Theme.INK : Color.WHITE);
        tabRegister.setBackground(reg ? Theme.NAVY : Theme.CARD);
        tabRegister.setForeground(reg ? Color.WHITE : Theme.INK);
        error.setText("");
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void submit() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        AuthService.AuthResult r = registerMode
                ? auth.register(nameField.getText().trim(), email, pass)
                : auth.login(email, pass);
        if (r.error != null) {
            error.setText(r.error);
            return;
        }
        user = r.user;
        token = r.token;
        dispose();
    }

    public User getUser() { return user; }
    public String getToken() { return token; }
}
