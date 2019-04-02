package io.data2viz.kotlinx.htmlplugin

import com.intellij.testFramework.LightPlatform4TestCase
import io.data2viz.kotlinx.htmlplugin.conversion.data.HtmlElement
import io.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import java.io.File

abstract class ResourcesTest : LightPlatform4TestCase() {


    protected fun loadHtmlData(filenameHtml: String): List<HtmlElement> {

        val htmlText = loadFileText(filenameHtml)


        val psiHtmlFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(ourProject, filenameHtml, htmlText)

        val htmlTag = HtmlPsiToHtmlDataConverter.convertPsiFileToHtmlTag(psiHtmlFile)

        return htmlTag

    }

    protected fun loadFileText(filename: String): String {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(filename)!!.file)
        return file.readText()
    }
}