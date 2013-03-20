package org.pillarone.riskanalytics.graph.core.layout

import org.pillarone.riskanalytics.graph.core.graph.persistence.Node

class ComponentLayout {

    Node node

    int x
    int y
    int width
    int height

    //TODO
//    boolean unfolded

    static belongsTo = [graphLayout: GraphLayout]


    static constraints = {
        x(nullable: true)
        y(nullable: true)
        width(nullable: true)
        height(nullable: true)
    }
}
