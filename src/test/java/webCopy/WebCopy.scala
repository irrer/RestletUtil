/*
 * Copyright 2021 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package webCopy

import org.restlet.data.MediaType
import org.restlet.resource.ClientResource
import org.restlet.Client
import org.restlet.data.Protocol
import org.restlet.representation.Representation
import scala.xml.PrettyPrinter
import scala.xml.Node
import scala.xml.XML
import java.io.ByteArrayOutputStream
import scala.Left
import scala.Right
import scala.xml.Elem
import scala.xml.MetaData
import edu.umro.util.Utility
import java.io.InputStream
import java.io.FileOutputStream

object WebCopy {

    def xmlToText(node: Node): String = new PrettyPrinter(1024, 2).format(node)

    def getByteArray(rep: Representation): Array[Byte] = {
        if ((rep.isEmpty) || (!rep.isAvailable)) Array[Byte]()
        else {
            val baos = new ByteArrayOutputStream
            rep.write(baos)
            baos.toByteArray
        }
    }

    private val kludgeList = List(
        ("&nbsp;", "@@nbsp%%"),
        //("&amp;", "@@amp%%"),
        ("<b>", "@@beginb%%"),
        ("""</b>""", "@@endb%%"),
        ("<code>", "@@begincode%%"),
        ("""</code>""", "@@endcode%%"))

    def kludge(txt: String, index: Int): String = {
        if (index == kludgeList.size) txt
        else kludge(txt.replace(kludgeList(index)._1, kludgeList(index)._2), index + 1)
    }

    def unKludge(txt: String, index: Int): String = {
        if (index == kludgeList.size) txt
        else unKludge(txt.replace(kludgeList(index)._2, kludgeList(index)._1), index + 1)
    }

    class ContentHtml(val title: String, val rep: Representation) {
        private val originalText = new String(getByteArray(rep))

        val htmlText = {
            val t = kludge(originalText.trim, 0)

            if (t.startsWith("<!")) t.substring(t.indexOf(">") + 1)
            else t
        }

        val xml = XML.loadString(htmlText)

        override def toString = unKludge(Config.pagePrefix + xmlToText(xml), 0)

        //        private def getInternalUrls(x: Node): Set[String] = {
        //            def getAttr(n: Node): Set[String] = n.attributes.map(a => a.key).map(key => x.attributes.get(key).get.head.toString).toSet.filter(v => v.toLowerCase.contains(Config.urlPrefix))
        //            getAttr(x) ++ x.child.foldLeft(Set[String]())((s, c) => s ++ getInternalUrls(c))
        //        }
        //        val internalUrls: Set[String] = getInternalUrls(xml).map(u => u.replace(Config.redundantPrefix, Config.urlPrefix))
        //        if (internalUrls.nonEmpty) { // TODO rm
        //            println("internalUrls")
        //            internalUrls.map(u => println("    " + u))
        //        }
        //println("htmlText:\n" + htmlText)

        def writeHtml = {
            val file = Config.pageTitleToFile(title)
            file.delete
            file.createNewFile
            val base = ""
            val ts = toString // TODO rm
            val text = toString
//                .replace("""<link href="/rodevwiki/index.php?title=MediaWiki:Common.css&usemsgcache=yes&ctype=text%2Fcss&smaxage=18000&action=raw&maxage=18000" rel="stylesheet"/>""", "<link href='../skins/Common.css' rel='stylesheet'/>")
//                .replace("""<link rel="stylesheet" href="/rodevwiki/index.php?title=MediaWiki:Print.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=18000&amp;action=raw&amp;maxage=18000" media="" />""", "<link href='../skins/Print.css' rel='stylesheet'/>")
//                .replace("""<link rel="stylesheet" href="/rodevwiki/index.php?title=MediaWiki:Monobook.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=18000&amp;action=raw&amp;maxage=18000" />""", "<link href='../skins/Monobook.css' rel='stylesheet'/>")
//                .replace("""<link rel="stylesheet" href="/rodevwiki/index.php?title=-&amp;action=raw&amp;maxage=18000&amp;gen=css" />""", "<link href='../skins/GeneratedUserStyleSheet.css' rel='stylesheet'/>")
                
                .replace("""<link href="/rodevwiki/index.php?title=MediaWiki:Common.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=18000&amp;action=raw&amp;maxage=18000" rel="stylesheet"/>""", "<link href='../skins/Common.css' rel='stylesheet'/>")
                .replace("""<link media="" href="/rodevwiki/index.php?title=MediaWiki:Print.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=18000&amp;action=raw&amp;maxage=18000" rel="stylesheet"/>""", "<link href='../skins/Print.css' rel='stylesheet'/>")
                .replace("""<link href="/rodevwiki/index.php?title=MediaWiki:Monobook.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=18000&amp;action=raw&amp;maxage=18000" rel="stylesheet"/>""", "<link href='../skins/Monobook.css' rel='stylesheet'/>")
                .replace("""<link href="/rodevwiki/index.php?title=-&amp;action=raw&amp;maxage=18000&amp;gen=css" rel="stylesheet"/>""", "<link href='../skins/GeneratedUserStyleSheet.css' rel='stylesheet'/>")

                
                .replace("""<script src="/rodevwiki/skins/common/wikibits.js?270"/>""", "<script src='../skins/common/wikibits.js'></script>")
                .replace("""<script src="/rodevwiki/skins/common/ajax.js?270"/>""", "<script src='../skins/common/ajax.js'></script>")
                .replace("""<script src="/rodevwiki/index.php?title=-&amp;action=raw&amp;gen=js&amp;useskin=monobook&amp;270"/>""", """<!-- <script src="/rodevwiki/index.php?title=-&amp;action=raw&amp;gen=js&amp;useskin=monobook&amp;270"/> -->""")
                
                
                .replace("http://141.214.124.203/rodevwiki/index.php/", "")
                .replace("/rodevwiki/index.php/", "")
                .replace("/rodevwiki/images", "../images")
                .replace("&amp;", "&")
                .replace("/rodevwiki/skins", "../skins")
                .replace("http://141.214.124.203/umdocs", "../umdocs")

            val fos = new FileOutputStream(file)
            fos.write(text.getBytes)
            fos.close
        }

        writeHtml
    }

    class ContentData(val path: String, val rep: Representation) {
        val data = getByteArray(rep)
    }

    private def closeRep(rep: Representation): Unit = {
        try {
            rep.getStream.close
        }
        catch {
            case t: Throwable => ;
        }
    }

    private def getContent(title: String): Either[ContentData, ContentHtml] = {
        val client = new Client(Protocol.HTTP)
        val path = Config.pageTitleToURL(title)
        val clientResource = new ClientResource(path)
        clientResource.setNext(client)
        val response = clientResource.getResponse
        println("HTTP GET: " + path)
        val rep = clientResource.get
        val mt = rep.getMediaType
        val content = rep.getMediaType match {
            case MediaType.TEXT_HTML => Right(new ContentHtml(title, rep))
            case _ => Left(new ContentData(path, rep))
        }
        closeRep(rep)
        content
    }

    /**
     * For testing and development only.
     */
    def main(args: Array[String]): Unit = {
        println("Starting")
        val start = System.currentTimeMillis

        try {
            //Utility.deleteFileTree(Config.outputDir)

            Config.outputDir.mkdirs
            Thread.sleep(500)

            val pageList = Config.pageTitleList.map(t => getContent(t).right.get)

            //            val internUrlList = pageList.map(p => p.internalUrls).flatten.toSet
            //            val distinctUrlList = internUrlList.map(u => u.replace("#.*", ""))

            //            internUrlList.map(u => println("       url: " + u))
            //            distinctUrlList.map(u => println("    disurl: " + u))

            //            println("  internUrlList.size: " + internUrlList.size)
            //            println("distinctUrlList.size: " + distinctUrlList.size)

            //   pageList.map(p => 

            //val title = "Convolution Release Notes"
            // val title = "Main Page"
            //        val title = "Dicom_Export_Project_Page"
            //        val cont1 = getContent(Config.pageTitleToURL(title))
            // println("title: " + title + "\n" + cont1.right.get)
            //  println("title: " + title)
            //   println("internalUrls: " + cont1.right.get.internalUrls.map(u => println("    " + u)))

            //        val url1 = "http://www-personal.umich.edu/~irrer/tmp/"
            //        val url2 = "http://141.214.124.203/rodevwiki/index.php/Convolution_Release_Notes"
            //        val url3 = "http://www-personal.umich.edu/~irrer/tmp/arrow-down.gif"
            //
            //        val res1 = get(url1)
            //        val res2 = get(url2)
            //        val res3 = get(url3)
            //        
            //        println("url2:\n" + res2.right.get)

        }
        catch {
            case t: Throwable => t.printStackTrace
        }
        println("\nExiting.  Elapsed ms: " + (System.currentTimeMillis - start))
        System.exit(0)
    }

}