import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

public class inkscapeSlider {

    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(args[0]));
            String svgFile = properties.getProperty("baseSvg");
            Integer numberOfSlides = Integer.parseInt(properties.getProperty("numberOfSlides"));
            for (int i = 1; i <= numberOfSlides ; i++) {
                createSvgForSlide(svgFile, properties.getProperty("slide"+i).split(","), i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createSvgForSlide(String svgFile, String[] layers, int slideNumber) {
        System.err.println("Creating Slide " + slideNumber);
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(svgFile);
            NodeList elements = document.getElementsByTagName("g");

            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                assert node instanceof Element;
                Element element = (Element) node;

                if (!element.getAttribute("inkscape:groupmode").equals("layer")) {
                    continue;
                }

                String label = element.getAttribute("inkscape:label");
                if (!Arrays.asList(layers).contains(label)) {
                    System.err.println("Removing layer " + label);
                    element.getParentNode().removeChild(element);
                    i--; // step back to process the child that got moved in place of the just removed one
                } else {
                    // ensure layer is visible!
                    String style = element.getAttribute("style");
                    if (style.equals("display:none")) {
                        System.err.println("Making layer " + label + " visible");
                        element.setAttribute("style", "display:inline");
                    }
                }
            }

            String target = svgFile.replace(".svg", "-" + slideNumber + ".svg");
            writeSvgFile(document, new File(target));
            System.err.println();
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void writeSvgFile(Document document, File output)
            throws TransformerException, UnsupportedEncodingException {
        System.err.println("Writing svg " + output);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(output));
    }
}