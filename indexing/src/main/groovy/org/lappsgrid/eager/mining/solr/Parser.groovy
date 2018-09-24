package org.lappsgrid.eager.mining.solr

import org.apache.solr.common.SolrInputDocument
import org.lappgrid.eager.core.solr.LappsDocument
import org.lappsgrid.eager.mining.solr.api.Worker
import org.lappsgrid.eager.mining.solr.parser.XmlDocumentExtractor

import java.util.concurrent.BlockingQueue

/**
 * Parses XML documents from the input queue and adds SolrInputDocuments on the
 * output queue.
 */
class Parser extends Worker {

    XmlParser parser
    XmlDocumentExtractor extractor
    int parserId
    int count

    public Parser(int id, BlockingQueue<Object> input, BlockingQueue<Object> output, XmlDocumentExtractor extractor) {
        super("Parser$id", input, output)
        this.parserId = id
        this.extractor = extractor
        parser = new XmlParser();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        this.count = 0
    }

    Object work(Object item) {
        File file = (File) item
        println "${++count} Parser $parserId: parsing ${file.name}"
        Node article = parser.parse(file)
        /*
        Node meta = article.front.'article-meta'[0]
        String pmid = getIdValue(meta, 'pmid')
        String pmc = getIdValue(meta, 'pmc')
        String doi = getIdValue(meta, 'doi')
        String title = meta.'title-group'.'article-title'.text()
        String abs = meta.'abstract'.text()
        String year = '0'
        Node pubDate = meta.'pub-date'.find { it.@'pub-type' == 'ppub' }
        if (pubDate == null) {
            pubDate = meta.'pub-date'.find { it.@'pub-type' == 'epub' }
        }
        if (pubDate != null) {
            year = pubDate.year.text()
        }
        title = title.replaceAll('\n', ' ').replaceAll('\r', ' ').replaceAll('\\s\\s+', ' ',)

        String id = getId(pmid, pmc, doi)
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", id)
        document.addField("pmid", pmid);
        document.addField("pmc", pmc);
        document.addField("doi", doi);
        document.addField("title", title)
        document.addField("year", year as int)
        document.addField("abstract", abs)
        document.addField("body", article.body.text())
        document.addField("path", file.path)
        */
        LappsDocument document = extractor.extractValues(article)
        document.path(file.getPath()).id()
        return document
    }

    String getIdValue(Node node, String id) {
        Node result = node.'article-id'.find { it.@'pub-id-type' == id }
        if (result == null) {
            return ""
        }
        def value = result.value()
        if (value == null) {
            return ""
        }
        return value[0]
    }

    String getId(String pmid, String pmc, String doi) {
        if (pmid) return pmid
        if (pmc) return pmc
        if (doi) return doi
        return UUID.randomUUID();
    }

}
/*
class Parser extends Haltable {

    public static final SolrInputDocument DONE = new SolrInputDocument()

    int id
    BlockingQueue<File> input;
    BlockingQueue<SolrInputDocument> output;

    XmlParser parser

    public Parser(int id, BlockingQueue<File> input, BlockingQueue<SolrInputDocument> output) {
        this.id = id
        this.input = input
        this.output = output
    }

    void run() {
        println "Parser $id thread starting"
        XmlParser parser = new XmlParser();
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        running = true
        while (running) {
            try {
                File file = input.take()
                if (file == DirectoryLister.DONE) {
                    running = false
                    output.put(DONE)
                    continue
                }
                println "Parser $id: parsing ${file.name}"
                Node article = parser.parse(file)
                Node meta = article.front.'article-meta'[0]
//                String pmid = meta.'article-id'.find { it.@'pub-id-type' == 'pmid' }?.value()[0]
//                String pmc = meta.'article-id'.find { it.@'pub-id-type' == 'pmc' }?.value()[0]
//                String doi = meta.'article-id'.find { it.@'pub-id-type' == 'doi' }?.value()[0]
                String pmid = getId(meta, 'pmid')
                String pmc = getId(meta, 'pmc')
                String doi = getId(meta, 'doi')
                String title = meta.'title-group'.'article-title'.text()
                String year = '0'
                Node pubDate = meta.'pub-date'.find { it.@'pub-type' == 'ppub' }
                if (pubDate == null) {
                    pubDate = meta.'pub-date'.find { it.@'pub-type' == 'epub' }
                }
                if (pubDate != null) {
                    year = pubDate.year.text()
                }
                title = title.replaceAll('\n', ' ').replaceAll('\r', ' ').replaceAll('\\s\\s+', ' ',)

                SolrInputDocument document = new SolrInputDocument();
                document.addField("pmid", pmid);
                document.addField("pmc", pmc);
                document.addField("doi", doi);
                document.addField("title", title)
                document.addField("year", year as int)
                document.addField("body", article.body.text())
                output.put(document)
            }
            catch (InterruptedException e) {
                println "Parser $id has been interrupted"
                running = false
                Thread.currentThread().interrupt()
            }
        }
        println "Parser $id thread terminating"
    }

    String getId(Node node, String id) {
        Node result = node.'article-id'.find { it.@'pub-id-type' == id }
        if (result == null) {
            return ""
        }
        def value = result.value()
        if (value == null) {
            return ""
        }
        return value[0]
    }
}
*/