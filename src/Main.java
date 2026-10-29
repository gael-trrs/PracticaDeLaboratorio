import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Main extends JFrame {
    private SVGPanel svgPanel;

    public Main() {
        setTitle("Visor Gráfico SVG con DOM");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        svgPanel = new SVGPanel();
        add(new JScrollPane(svgPanel), BorderLayout.CENTER);

        JButton btnOpen = new JButton("Seleccionar archivo SVG...");
        btnOpen.addActionListener(e -> openFile());

        JPanel topPanel = new JPanel();
        topPanel.add(btnOpen);
        add(topPanel, BorderLayout.NORTH);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                List<SVGShape> shapes = SVGParser.parse(file);
                svgPanel.setShapes(shapes);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar el documento XML: " + ex.getMessage(), "Error de lectura", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

class SVGPanel extends JPanel {
    private List<SVGShape> shapes = new ArrayList<>();

    public void setShapes(List<SVGShape> shapes) {
        this.shapes = shapes;
        setPreferredSize(new Dimension(1000, 1000));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (SVGShape shape : shapes) {
            shape.draw(g2d);
        }
    }
}

class SVGParser {
    public static List<SVGShape> parse(File file) throws Exception {
        List<SVGShape> shapes = new ArrayList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setExpandEntityReferences(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        parseNode(doc.getDocumentElement(), shapes);
        return shapes;
    }

    private static void parseNode(Node node, List<SVGShape> shapes) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) node;
            String tagName = elem.getTagName();

            SVGShape shape = null;
            switch (tagName) {
                case "line": shape = new SVGLine(elem); break;
                case "circle": shape = new SVGCircle(elem); break;
                case "polyline": shape = new SVGPolyline(elem); break;
                case "rect": shape = new SVGRect(elem); break;
                case "ellipse": shape = new SVGEllipse(elem); break;
                case "text": shape = new SVGTextNode(elem); break;
            }
            if (shape != null) shapes.add(shape);
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            parseNode(children.item(i), shapes);
        }
    }
}

abstract class SVGShape {
    protected Color fill = null;
    protected Color stroke = null;
    protected float strokeWidth = 1.0f;

    public SVGShape(Element elem) {
        String fillAttr = elem.getAttribute("fill");
        if (!fillAttr.isEmpty() && !fillAttr.equals("none")) {
            fill = parseColor(fillAttr);
        }

        String strokeAttr = elem.getAttribute("stroke");
        if (!strokeAttr.isEmpty() && !strokeAttr.equals("none")) {
            stroke = parseColor(strokeAttr);
        } else if (strokeAttr.isEmpty() && this instanceof SVGLine) {
            stroke = Color.BLACK;
        }

        if (fillAttr.isEmpty() && !(this instanceof SVGLine) && !(this instanceof SVGPolyline)) {
            fill = Color.BLACK;
        }

        String strokeWidthAttr = elem.getAttribute("stroke-width");
        if (!strokeWidthAttr.isEmpty()) {
            strokeWidth = (float) parseDouble(strokeWidthAttr, 1.0);
        }
    }

    protected Color parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("#")) return Color.decode(colorStr);
            switch (colorStr.toLowerCase()) {
                case "red": return Color.RED;
                case "blue": return Color.BLUE;
                case "green": return Color.GREEN;
                case "yellow": return Color.YELLOW;
                case "black": return Color.BLACK;
                case "white": return Color.WHITE;
                case "orange": return Color.ORANGE;
                case "pink": return Color.PINK;
                case "cyan": return Color.CYAN;
                case "magenta": return Color.MAGENTA;
                case "lightgray": return Color.LIGHT_GRAY;
                case "lightblue": return new Color(173, 216, 230);
                case "lightgreen": return new Color(144, 238, 144);
                case "navy": return new Color(0, 0, 128);
                default: return Color.BLACK;
            }
        } catch (Exception e) { return Color.BLACK; }
    }

    protected double parseDouble(String val, double defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); } catch(Exception e) { return defaultVal; }
    }

    protected void applyStylesAndDraw(Graphics2D g2d, Shape shape) {
        if (fill != null) {
            g2d.setColor(fill);
            g2d.fill(shape);
        }
        if (stroke != null) {
            g2d.setColor(stroke);
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.draw(shape);
        }
    }

    public abstract void draw(Graphics2D g2d);
}

class SVGLine extends SVGShape {
    private double x1, y1, x2, y2;
    public SVGLine(Element elem) {
        super(elem);
        x1 = parseDouble(elem.getAttribute("x1"), 0);
        y1 = parseDouble(elem.getAttribute("y1"), 0);
        x2 = parseDouble(elem.getAttribute("x2"), 0);
        y2 = parseDouble(elem.getAttribute("y2"), 0);
    }
    @Override
    public void draw(Graphics2D g2d) { applyStylesAndDraw(g2d, new Line2D.Double(x1, y1, x2, y2)); }
}

class SVGCircle extends SVGShape {
    private double cx, cy, r;
    public SVGCircle(Element elem) {
        super(elem);
        cx = parseDouble(elem.getAttribute("cx"), 0);
        cy = parseDouble(elem.getAttribute("cy"), 0);
        r = parseDouble(elem.getAttribute("r"), 0);
    }
    @Override
    public void draw(Graphics2D g2d) { applyStylesAndDraw(g2d, new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2)); }
}

class SVGPolyline extends SVGShape {
    private Path2D.Double path = new Path2D.Double();
    public SVGPolyline(Element elem) {
        super(elem);
        String pointsStr = elem.getAttribute("points");
        String[] pairs = pointsStr.trim().split("\\s+");
        boolean first = true;
        for (String pair : pairs) {
            String[] coords = pair.split(",");
            if (coords.length == 2) {
                double px = parseDouble(coords[0], 0);
                double py = parseDouble(coords[1], 0);
                if (first) { path.moveTo(px, py); first = false; }
                else { path.lineTo(px, py); }
            }
        }
    }
    @Override
    public void draw(Graphics2D g2d) { applyStylesAndDraw(g2d, path); }
}

class SVGRect extends SVGShape {
    private double x, y, width, height;
    public SVGRect(Element elem) {
        super(elem);
        x = parseDouble(elem.getAttribute("x"), 0);
        y = parseDouble(elem.getAttribute("y"), 0);
        width = parseDouble(elem.getAttribute("width"), 0);
        height = parseDouble(elem.getAttribute("height"), 0);
    }
    @Override
    public void draw(Graphics2D g2d) { applyStylesAndDraw(g2d, new Rectangle2D.Double(x, y, width, height)); }
}

class SVGEllipse extends SVGShape {
    private double cx, cy, rx, ry;
    public SVGEllipse(Element elem) {
        super(elem);
        cx = parseDouble(elem.getAttribute("cx"), 0);
        cy = parseDouble(elem.getAttribute("cy"), 0);
        rx = parseDouble(elem.getAttribute("rx"), 0);
        ry = parseDouble(elem.getAttribute("ry"), 0);
    }
    @Override
    public void draw(Graphics2D g2d) { applyStylesAndDraw(g2d, new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); }
}

class SVGTextNode extends SVGShape {
    private double x, y;
    private String text;
    private String fontFamily = "SansSerif";
    private int fontSize = 12;

    public SVGTextNode(Element elem) {
        super(elem);
        x = parseDouble(elem.getAttribute("x"), 0);
        y = parseDouble(elem.getAttribute("y"), 0);
        text = elem.getTextContent();

        String ffAttr = elem.getAttribute("font-family");
        if (!ffAttr.isEmpty()) fontFamily = ffAttr;

        String fsAttr = elem.getAttribute("font-size");
        if (!fsAttr.isEmpty()) fontSize = (int)parseDouble(fsAttr, 12);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
        g2d.setColor(fill != null ? fill : Color.BLACK);
        g2d.drawString(text, (float) x, (float) y);
    }
}