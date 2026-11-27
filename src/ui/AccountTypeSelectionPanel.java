package ui;

import model.UserType;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Public account type chooser; Administrator accounts are intentionally not offered. */
public class AccountTypeSelectionPanel extends JPanel {
    private UserType selected;
    private final JButton continueButton=Theme.primaryButton("Continue");
    public AccountTypeSelectionPanel(Runnable back, Consumer<UserType> proceed) {
        setLayout(new BorderLayout(12,12)); setBackground(Theme.BACKGROUND);
        JLabel title=new JLabel("Choose an account type",SwingConstants.CENTER); title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.PRIMARY_NAVY);
        JLabel subtitle=new JLabel("Select the option that matches your campus role.",SwingConstants.CENTER); subtitle.setForeground(Theme.TEXT_MUTED);
        JPanel north=new JPanel(new GridLayout(2,1,0,6)); north.setOpaque(false); north.add(title);north.add(subtitle); add(north,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,3,16,0));cards.setOpaque(false);cards.setBorder(BorderFactory.createEmptyBorder(25,32,25,32));
        ButtonGroup group=new ButtonGroup();
        addChoice(cards,group,"🎓 Student","Student information and initial vehicle",UserType.STUDENT);
        addChoice(cards,group,"🧑‍💼 School Personnel","Employee information and initial vehicle",UserType.SCHOOL_PERSONNEL);
        addChoice(cards,group,"\uD83E\uDDF3 Visitor","Visitor account and parking reservation",UserType.VISITOR);
        add(cards,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.setOpaque(false);
        JButton backButton=Theme.secondaryButton("Back to Login"); continueButton.setEnabled(false);
        backButton.addActionListener(e->back.run());continueButton.addActionListener(e->{if(selected!=null)proceed.accept(selected);});
        actions.add(backButton);actions.add(continueButton);add(actions,BorderLayout.SOUTH);
    }
    private void addChoice(JPanel cards,ButtonGroup group,String name,String detail,UserType type){
        JToggleButton choice=new JToggleButton("<html><center><div style='font-size:18pt'>"+name+"</div><br>"+detail+"</center></html>");
        choice.setFont(Theme.FONT_LABEL);choice.setForeground(Theme.TEXT_DARK);choice.setBackground(Theme.CARD_WHITE);choice.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT,2));choice.setFocusPainted(false);
        choice.addActionListener(e->{selected=type; continueButton.setEnabled(true); });
        group.add(choice);cards.add(choice);
    }
}
