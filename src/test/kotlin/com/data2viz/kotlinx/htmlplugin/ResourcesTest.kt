package com.data2viz.kotlinx.htmlplugin

import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlElement
import com.data2viz.kotlinx.htmlplugin.conversion.data.HtmlTag
import com.data2viz.kotlinx.htmlplugin.conversion.model.HtmlPsiToHtmlDataConverter
import com.data2viz.kotlinx.htmlplugin.conversion.model.toKotlinX
import com.intellij.ide.highlighter.ProjectFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectImpl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.LightPlatform4TestCase
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.PlatformTestCase
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.annotations.NonNls
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayOutputStream

import java.io.File
import java.io.PrintStream

open class ResourcesTest : LightPlatform4TestCase() {


    protected fun loadHtmlData(filenameHtml: String): List<HtmlElement> {

        val htmlText = loadFileText(filenameHtml)


        val psiHtmlFile = HtmlPsiToHtmlDataConverter.createHtmlFileFromText(ourProject, filenameHtml, htmlText);

        val htmlTag = HtmlPsiToHtmlDataConverter.convertPsiFileToHtmlTag(psiHtmlFile);

        return htmlTag

    }

    protected fun loadFileText(filename: String): String {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(filename)!!.file)
        return file.readText();
    }
}