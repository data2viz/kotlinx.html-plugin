package io.data2viz.kotlinx.htmlplugin

import com.intellij.testFramework.LightPlatform4TestCase
import io.data2viz.kotlinx.htmlplugin.conversion.HtmlElement
import io.data2viz.kotlinx.htmlplugin.conversion.HtmlPsiToHtmlDataConverter
import java.io.File

abstract class ResourcesTest : LightPlatform4TestCase() {


    protected fun loadHtmlData(filenameHtml: String): List<HtmlElement> {

        val htmlText = loadFileText(filenameHtml)

        val psiHtmlFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(project, filenameHtml, htmlText)

        return HtmlPsiToHtmlDataConverter.convertPsiFileToHtmlTag(psiHtmlFile)

    }

    protected fun loadFileText(filename: String): String {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(filename)!!.file)
        return file.readText()
    }
}