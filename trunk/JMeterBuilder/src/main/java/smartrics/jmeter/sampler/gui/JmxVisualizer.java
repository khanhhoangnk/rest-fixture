/*  Copyright 2009 Fabrizio Cannizzo
 *
 *  This file is part of JMeterRestSampler.
 *
 *  JMeterRestSampler (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  JMeterRestSampler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JMeterRestSampler.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please see http://smartrics.blogspot.com
 */
package smartrics.jmeter.sampler.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import smartrics.jmeter.sampler.JmxSampleResult;

/**
 * Visualises the results of sampling a JMX server for memory.
 *
 * Each server is represented by a graph and identified by it's uri. It captures
 * and display results from all JmxSampler set up to sample different servers.
 */
@SuppressWarnings("serial")
public class JmxVisualizer extends AbstractVisualizer {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private VerticalPanel graphsPanel;
    private Map<String, JmxGraphPanel> graphTable = Collections.synchronizedMap(new HashMap<String, JmxGraphPanel>());

    public JmxVisualizer() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        VerticalPanel groupPanel = new VerticalPanel();
        Container titlePanel = makeTitlePanel();
        groupPanel.add(titlePanel);
        add(groupPanel, BorderLayout.NORTH);
        graphsPanel = new VerticalPanel();
        add(graphsPanel, BorderLayout.CENTER);
    }

    public String getStaticLabel() {
        return "JMX Memory Usage";
    }

    private JmxGraphPanel addGraph(String memType, String uri) {
        JmxGraphPanel graphPanel = new JmxGraphPanel();
        ChartWrapper graph = new ChartWrapper();
        graphsPanel.add(graphPanel);
        graphPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        graph.setTitle(memType + " usage for " + uri);
        graph.setXAxisTitle("elapsed time (seconds)");
        graph.setYAxisTitle("Kb");
        graph.setYAxisLabels(new String[] { memType, "Average" });
        Dimension size = new Dimension(800, 600);
        graph.setWidth(size.width);
        graph.setHeight(size.height);
        graphPanel.setGraph(graph);
        graphPanel.setMaximumSize(size);
        graphPanel.setPreferredSize(size);
        return graphPanel;
    }

    public void add(SampleResult res) {
        if (res instanceof JmxSampleResult) {
            log.warn("INVOKED: " + res);
            JmxSampleResult result = (JmxSampleResult) res;
            String uri = result.getJmxUri();
            JmxGraphPanel graphPanel = graphTable.get(uri);
            if (graphPanel == null) {
                graphPanel = addGraph(result.getMemType(), uri);
                graphTable.put(uri, graphPanel);
            }
            ChartWrapper graph = graphPanel.getGraph();
            graph.setXAxisScalingFactor(1000);
            graph.putRawData(result.getStartTime(), result.getValue());
            renderChart(uri, graphPanel);
            if (result.isSaveGraph()) {
                graph.saveGraph(result.getGraphFilename());
            }
        }
    }

    public String getLabelResource() {
        return "jmx.visualizer";
    }

    private synchronized void renderChart(String uri, JmxGraphPanel graphPanel) {
        graphPanel.invalidate();
        graphPanel.paintComponent();
        repaint();
    }

    public void clearData() {
        // TODO Auto-generated method stub

    }
}
