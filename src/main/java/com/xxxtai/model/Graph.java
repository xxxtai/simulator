package com.xxxtai.model;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

@Component
public class Graph {
    private ArrayList<Node> nodeArray;
    private ArrayList<Edge> edgeArray;

    public Graph() {
        nodeArray = new ArrayList<>();
        edgeArray = new ArrayList<>();
        importNewGraph();
    }

    private void importNewGraph() {
        File file = new File("C:\\Users\\xxxta\\work\\Graph.xls");
        try {
            System.out.println(file.getPath());
            InputStream is = new FileInputStream(file.getPath());//this.getClass().getResourceAsStream("/testGraph.xls");
            Workbook wb = Workbook.getWorkbook(is);

            Sheet sheetNodes = wb.getSheet("nodes");
            for (int i = 0; i < sheetNodes.getRows(); i++) {
                int x = 0, y = 0, cardNum = 0;
                for (int j = 0; j < 4; j++) {
                    Cell cell0 = sheetNodes.getCell(j, i);
                    String str = cell0.getContents();
                    if (j == 0)
                        cardNum = Integer.parseInt(str);
                    if (j == 1)
                        x = Integer.parseInt(str);
                    if (j == 2)
                        y = Integer.parseInt(str);

                }
                this.addImportNode(cardNum, x, y);
            }

            Sheet sheetEdges = wb.getSheet("edges");
            for (int i = 0; i < sheetEdges.getRows(); i++) {
                int start = 0, end = 0, dis = 0, CardNum = 0;
                for (int j = 0; j < 4; j++) {
                    Cell cell0 = sheetEdges.getCell(j, i);
                    String str = cell0.getContents();
                    if (j == 0)
                        start = Integer.parseInt(str);
                    if (j == 1)
                        end = Integer.parseInt(str);
                    if (j == 2)
                        dis = Integer.parseInt(str);
                    if (j == 3)
                        CardNum = Integer.parseInt(str);
                }
                this.addEdge(start, end, dis, CardNum);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNodeArraySize() {
        return this.nodeArray.size();
    }

    public ArrayList<Edge> getEdgeArray() {
        return this.edgeArray;
    }

    public Node getNode(int index) {
        return this.nodeArray.get(index);
    }

    Edge getEdge(int index) {
        return this.edgeArray.get(index);
    }

    private void addImportNode(int cardNum, int x, int y) {
        nodeArray.add(new Node(cardNum, x, y));
    }

    private void addEdge(int strNodeNum, int endNodeNum, int dis, int CardNum) {
        for (int i = 0; i < nodeArray.size(); i++) {
            if (nodeArray.get(i).CARD_NUM == strNodeNum)
                strNodeNum = i;
        }
        for (int i = 0; i < nodeArray.size(); i++) {
            if (nodeArray.get(i).CARD_NUM == endNodeNum)
                endNodeNum = i;
        }

        edgeArray.add(new Edge(nodeArray.get(strNodeNum), nodeArray.get(endNodeNum)
                , dis, CardNum));
    }
}
