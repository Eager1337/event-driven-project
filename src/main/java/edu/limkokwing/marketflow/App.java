package edu.limkokwing.marketflow;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public final class App {
    private App() {}
    static void configureUI() throws Exception {
        for(UIManager.LookAndFeelInfo info:UIManager.getInstalledLookAndFeels()) {
            if(info.getName().equals("Nimbus")){UIManager.setLookAndFeel(info.getClassName());break;}
        }
        for(Enumeration<Object> keys=UIManager.getDefaults().keys();keys.hasMoreElements();) {
            Object key=keys.nextElement(); if(UIManager.get(key) instanceof Font)UIManager.put(key,new FontUIResource("SansSerif",Font.PLAIN,14));
        }
        UIManager.put("Panel.background",new Color(245,247,250));
        UIManager.put("Table.selectionBackground",new Color(20,80,110));UIManager.put("Table.selectionForeground",Color.WHITE);
    }
    public static void main(String[] args) {
        if(args.length>0 && args[0].equals("--check-db")) {
            try{Database.configured().ping();System.out.println("Database connected; product schema is available.");}
            catch(Exception e){System.err.println("Database check failed: "+e.getMessage());System.exit(1);}
            return;
        }
        if(GraphicsEnvironment.isHeadless()) {System.err.println("MarketFlow needs a graphical desktop. Use mvn test for headless validation.");System.exit(1);}
        SwingUtilities.invokeLater(()->{
            try {
                configureUI();
                MainFrame frame=new MainFrame(new Store(Database.configured()));frame.setVisible(true);
            }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"MarketFlow setup required",JOptionPane.ERROR_MESSAGE);}
        });
    }
}