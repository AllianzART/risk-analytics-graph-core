package org.pillarone.riskanalytics.graph.core.graphimport

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.wiring.ITransmitter
import org.pillarone.riskanalytics.core.wiring.WiringUtils
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService
import org.pillarone.riskanalytics.graph.core.graph.model.Port
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.pillarone.riskanalytics.graph.core.graph.model.Connection

public abstract class AbstractGraphImport {

    protected CommentImport commentImport;

    public abstract AbstractGraphModel importGraph(Class clazz, String content)

    ;

    protected HashMap<Component, ComponentNode> getComponents(Object o, AbstractGraphModel graph) {

        HashMap<Component, ComponentNode> components = new HashMap<Component, ComponentNode>();
        for (MetaProperty mp: o.metaClass.getProperties()) {
            if (Component.isAssignableFrom(mp.type)) {
                ComponentNode n = graph.createComponentNode(PaletteService.getInstance().getComponentDefinition(mp.type), mp.name);
                commentImport.getComponentComment(n);
                components.put(DefaultGroovyMethods.getAt(o, mp.name), n);
            }
        }
        return components;
    }


    protected void addConnections(AbstractGraphModel graph, HashMap<Component, ComponentNode> components) {
        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();

        for (Component c: components.keySet()) {
            List allTransmitter = new ArrayList();
            allTransmitter.addAll(c.getAllOutputTransmitter());
            allTransmitter.addAll(c.getAllInputTransmitter());
            for (ITransmitter t: allTransmitter) {
                String toPort = WiringUtils.getSenderChannelName(t.getReceiver(), t.getTarget());
                String fromPort = WiringUtils.getSenderChannelName(t.getSender(), t.getSource());
                ComponentNode toComp = components.get(t.getReceiver());
                ComponentNode fromComp = components.get(t.getSender());

                if (toComp == null || fromComp == null) {
                    continue;
                }

                String key = fromComp.name + "." + fromPort + "=" + toComp.name + "." + toPort;

                Port fromP = fromComp.outPorts.find {it.name.equals(fromPort)};
                Port toP = toComp.inPorts.find {it.name.equals(toPort)};

                if (fromP != null && toP != null) {
                    if (!visited.get(key)) {
                        Connection connection = graph.createConnection(fromP, toP);
                        commentImport.getConnectionComment(connection);
                        visited.put(key, true);
                    }
                }

            }
        }
    }


}

class CommentImport {
    private HashMap<String, String> comments = new HashMap<String, String>();

    public CommentImport(String content) {
        structureComments(content);
    }

    public String getComponentComment(ComponentNode componentNode) {
        return comments.get("Component:" + componentNode.name);
    }

    public String getConnectionComment(Connection connection) {
        return comments.get("Connection:" + connection.from.componentNode.name + "." + connection.from.name +
                "->" + connection.to.componentNode.name + "." + connection.to.name);
    }

    public String getReplicationComment(Connection connection, boolean direction) {
        if (direction)
            return comments.get("Replication:" + connection.from.name +
                    "->" + connection.to.componentNode.name + "." + connection.to.name);
        else
            return comments.get("Replication:" + connection.from.componentNode.name + "." + connection.from.name +
                    "->" + connection.to.name);
    }

    private void structureComments(String content) {
        Pattern p = Pattern.compile('/\\*+(.*?)\\*+/', Pattern.DOTALL);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String plain = m.group(1).replaceAll('\\*', '').trim();
            try {
                StringReader sr = new StringReader(plain);
                String header = sr.readLine().trim();
                String comment = sr.readLine().trim();
                comments.put(header, comment);
            } catch (Exception e) {
                continue;
            }
        }
    }
}