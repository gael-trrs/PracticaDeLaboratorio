# Práctica de laboratorio: Visor SVG con Java DOM y Java 2D

## Arquitectura y Decisiones de Implementación
La aplicación sigue una arquitectura orientada a objetos basada en el principio de Responsabilidad Única. Se separó la lógica de lectura y parseo XML (clase `SVGParser`) de la lógica de renderizado gráfico (`SVGPanel`).
Se creó una clase abstracta `SVGShape` que centraliza la extracción de atributos comunes como `fill`, `stroke` y `stroke-width`, y obliga a sus subclases a implementar el método `draw(Graphics2D g2d)`.

## Diagrama de Clases UML
*(Diagrama de la estructura orientada a objetos)*

```mermaid
classDiagram
    class SVGShape {
        <<abstract>>
        #Color fill
        #Color stroke
        #float strokeWidth
        +draw(Graphics2D g2d)*
        #parseColor(String) Color
        #applyStylesAndDraw(Graphics2D, Shape)
    }
    class SVGLine {
        -double x1, y1, x2, y2
        +draw(Graphics2D g2d)
    }
    class SVGCircle {
        -double cx, cy, r
        +draw(Graphics2D g2d)
    }
    class SVGRect {
        -double x, y, width, height
        +draw(Graphics2D g2d)
    }
    class SVGEllipse {
        -double cx, cy, rx, ry
        +draw(Graphics2D g2d)
    }
    class SVGPolyline {
        -Path2D.Double path
        +draw(Graphics2D g2d)
    }
    class SVGTextNode {
        -double x, y
        -String text, fontFamily
        -int fontSize
        +draw(Graphics2D g2d)
    }
    
    SVGShape <|-- SVGLine
    SVGShape <|-- SVGCircle
    SVGShape <|-- SVGRect
    SVGShape <|-- SVGEllipse
    SVGShape <|-- SVGPolyline
    SVGShape <|-- SVGTextNode

¿Qué ventajas y desventajas presenta DOM frente a SAX para procesar documentos SVG?Ventaja: DOM carga todo el documento en la memoria creando un árbol jerárquico, lo que facilita la navegación no secuencial y la lectura de nodos hijos de manera más sencilla.Desventaja: Consume más memoria RAM que SAX, ya que SAX procesa los eventos secuencialmente sin cargar todo a memoria.

¿Cómo se relaciona la estructura jerárquica de un documento XML con su representación mediante objetos Java?
Cada etiqueta XML (<circle>, <rect>) se convierte en la instancia de una clase Java (SVGCircle, SVGRect). Los atributos de la etiqueta XML (cx, fill) se guardan como variables de instancia dentro del objeto Java correspondiente.

¿Qué ventajas ofrece utilizar polimorfismo para implementar el renderizado de diferentes figuras?
Permite que la clase encargada de dibujar (SVGPanel) solo tenga que llamar al método .draw() sobre una lista genérica de objetos abstractos (SVGShape), sin tener que utilizar largas estructuras switch/case ni saber internamente cómo se dibuja cada figura específica.

¿Por qué es conveniente separar el procesamiento XML de la representación gráfica?
Para asegurar el acoplamiento bajo (Bajo Acoplamiento) y la alta cohesión. Si en el futuro cambiamos DOM por SAX, o Java2D por JavaFX, el código de la otra capa no requerirá modificaciones.

¿Qué dificultades aparecen al interpretar atributos geométricos expresados como cadenas de texto?
La validación de datos. Es muy común toparse con NumberFormatException por valores vacíos, nulos o con sufijos de unidades (ej. "10px" en lugar de "10"). Se requiere implementar mecanismos robustos de manejo de excepciones o valores por defecto.

¿Cómo podría ampliarse la arquitectura para incorporar nuevos elementos SVG?
Bastaría con crear una nueva clase Java (ej. SVGPolygon) que herede de SVGShape, implementar cómo se extraen sus atributos específicos, escribir su lógica dentro de draw(), y agregar un nuevo caso (case "polygon") en el analizador SVGParser.

¿Qué modificaciones serían necesarias para implementar transformaciones geométricas como traslación, rotación y escalamiento?
Se debería leer el atributo transform del XML, parsear su valor matricial y aplicar un objeto AffineTransform al contexto gráfico Graphics2D justo antes de ejecutar el dibujo de la figura y restaurar el contexto al terminar.