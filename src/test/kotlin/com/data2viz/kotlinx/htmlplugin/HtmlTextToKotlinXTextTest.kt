package com.data2viz.kotlinx.htmlplugin

import com.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import com.intellij.testFramework.LightPlatform4TestCase
import org.junit.Assert
import org.junit.Test

class HtmlTextToKotlinXTextTest: ResourcesTest() {


    @Test
     fun FileHtmlToKotlinXBase() {
        assertFiles("base.html", "base.htmlkotlinx")
    }

    private fun assertFiles(filenameHtml: String, filenameKotlinX: String) {

        val kotlinXText = loadFileText(filenameKotlinX)

        val htmlTags = loadHtmlData(filenameHtml)

        val convertedToKotlinXText = htmlTags.toKotlinX()

        Assert.assertEquals(kotlinXText, convertedToKotlinXText);

    }



}