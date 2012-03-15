<?xml version='1.0'?>

<!--
 $Id: site.src.xsl,v 1.5 2004/06/09 02:45:56 rcleveng Exp $
-->

<?xml-stylesheet type="text/xsl" href="./site.xsl" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.1'
                xmlns:xhtml="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default xsl xhtml">

<!-- load the main docbook stylesheet -->
<xsl:import href="@DIR.DOCBOOK.XSL@/html/docbook.xsl" />

<xsl:param name="html.stylesheet">site.css</xsl:param>
<xsl:param name="css.decoration">1</xsl:param>
<xsl:param name="section.label.includes.component.label" select="0" />
<xsl:param name="make.valid.html" select="1" />
<xsl:param name="html.cleanup" select="1" />
<xsl:param name="default.encoding" select="ISO-8859-1"/>
<xsl:param name="toc.list.type">ul</xsl:param>
<xsl:param name="funcsynopsis.style">ansi</xsl:param>
<xsl:param name="shade.verbatim" select="1"/>
<xsl:param name="formal.procedures" select="0" />
<xsl:param name="using.chunker" select="0" />
<xsl:param name="generate.toc" select="0" />
<xsl:param name="annotate.toc" select="0" />
<xsl:param name="generate.index" select="0" />
<xsl:param name="section.autolabel" select="0" />
<!-- <xsl:param name="use.extensions" select="1"/> -->
<xsl:param name="tablecolumns.extension" select="1"/>
<xsl:param name="table.borders.with.css" select="1"/>
<xsl:param name="table.border.style" select="'solid'"/>
<xsl:param name="table.border.thickness" select="'1px'"/>
<xsl:param name="table.border.color" select="'#336699'"/>
<xsl:param name="html.cellpadding" select="'2'"/>
<xsl:param name="html.cellspacing" select="'0'"/>

<xsl:param name="build.time" select="-1"/>

<xsl:attribute-set name="shade.verbatim.style">
  <xsl:attribute name="class">shade</xsl:attribute>
</xsl:attribute-set>

<xsl:template match="*" mode="process.root">
  <xsl:if test="$build.time='-1'">
    <xsl:message>ERROR: You must supply the stylesheet parameter "build.time"</xsl:message>
  </xsl:if>

  <xsl:variable name="doc" select="self::*" />
  <xsl:text disable-output-escaping="yes">
    <![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">]]>
  </xsl:text>

<html>
  <head>
    <xsl:call-template name="head.content">
      <xsl:with-param name="node" select="$doc" />
    </xsl:call-template>
    <xsl:text disable-output-escaping="yes">
      <![CDATA[<meta name="description" content="Jalopy Java Source Code Formatter Beautifier Pretty Printer"> ]]>
      <![CDATA[<meta http-equiv="pics-label" content='(pics-1.1 "http://www.icra.org/ratingsv02.html" l gen true for "http://jalopy.sf.net" r (cz 1 lz 1 nz 1 oz 1 vz 1) "http://www.rsac.org/ratingsv01.html" l gen true for "http://jalopy.sf.net" r (n 0 s 0 v 0 l 0))'> ]]>
    </xsl:text>
    <xsl:call-template name="user.head.content">
      <xsl:with-param name="node" select="$doc" />
    </xsl:call-template>
  </head>

  <!-- BODY STARTS HERE -->

  <body id="toppage">
    <xsl:call-template name="body.attributes" />
    <table width="700" border="0" cellpadding="0" cellspacing="0" align="center">
      <tbody>
        <tr>
          <td>

            <!-- NAVIGATION STARTS HERE -->

            <table cellpadding="0" cellspacing="0" width="100%" style="border:1px solid #336699">
              <tbody>
                <tr>
                  <td height="16">
                  </td>
                </tr>
                <tr>
                  <td bgcolor="#3399cc" height="1">
                  </td>
                </tr>
                <tr style="border:none">
                  <td style="border:none">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tbody>
                        <tr>
                          <td class="logo">JALOPY</td>
                          <td class="sublogo" valign="bottom">Java Source Code Formatter Beautifier Pretty Printer</td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
                <tr>
                  <td bgcolor="#3399cc" height="1">
                  </td>
                </tr>
                <tr>
                  <td height="10">
                  </td>
                </tr>
                <tr>
                  <td bgcolor="#ff8000" height="4">
                  </td>
                </tr>
                <tr>
                  <td height="20" bgcolor="#336699" style="color:#ffffff;padding-left:10px">
                    <a href="./index.html" class="navlink">Overview</a> &#149;
                    <a href="./download.html" class="navlink">Download</a> &#149;
                    <a href="./docs.html" class="navlink">Documentation</a> &#149;
                    <a href="./plugins.html" class="navlink">Plug-ins</a> &#149;
                    <a href="./links.html" class="navlink">Links</a> &#149;
                    <a href="./contact.html" class="navlink">Contact</a>
                  </td>
                </tr>
                <tr>
                  <td height="1" bgcolor="#ffffff">
                  </td>
                </tr>
              </tbody>
            </table>

            <xsl:choose>
              <xsl:when test="/article/@role='main'">

                <!-- add a navbar for the main section -->
                <table cellpadding="0" cellspacing="0" width="100%">
                  <tbody>
                    <tr>
                      <td height="20" bgcolor="#faebd7" style="padding-left:15px">
                        <a href="./features.html" class="navlink2">Features</a> |
                        <a href="./history.html" class="navlink2">History</a>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </xsl:when>
              <xsl:when test="/article/@role='docu'">

                <!-- add a navbar for the docu section -->
                <table cellpadding="0" cellspacing="0" width="100%">
                  <tbody>
                    <tr>
                      <td height="20" bgcolor="#faebd7" style="padding-left:15px">
                        <a href="./features.html" class="navlink2">Features</a> |
                        <a href="./history.html" class="navlink2">History</a> |
                        <a href="./manual.html" class="navlink2">Manual</a> |
                        <a href="./faq.html" class="navlink2">FAQ</a> |
                        <a href="./api/index.html" class="navlink2">Javadoc</a>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </xsl:when>
            </xsl:choose>


            <!-- NAVIGATION ENDS HERE -->

          </td>
        </tr>
        <tr>
          <td height="20" bgcolor="#ffffff">
          </td>
        </tr>
        <tr>
          <td bgcolor="#eeeecc" height="17" align="right" style="font-size:10px;padding-right:3px">
            This page generated: <strong><xsl:value-of select="$build.time"/></strong>
          </td>
        </tr>
        <tr valign="top">
          <td valign="top" bgcolor="#ffffff"> <!-- fffff0 -->

            <!-- CONTENT STARTS HERE -->

            <table border="0" width="100%" cellspacing="0" cellpadding="5">
              <tbody>
                <tr>
                  <td>
                    <xsl:apply-templates select="." />
                  </td>
                </tr>
                <tr>
                  <td height="20">
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- CONTENT ENDS HERE -->

          </td>
        </tr>
        <tr>
          <td bgcolor="#eeeecc" height="17" style="font-size:9px;padding-left:5px">
            <a href="#toppage">to top</a>
          </td>
        </tr>
        <tr>
          <td height="30">
            <br/>
          </td>
        </tr>
        <tr>
          <td height="3">
          </td>
        </tr>
        <tr>
          <td bgcolor="#336699" height="1">
          </td>
        </tr>
        <tr>
          <td height="1">
          </td>
        </tr>
        <tr>
          <td bgcolor="#336699" height="16">
          </td>
        </tr>
        <tr>
          <td bgcolor="#ff9966" height="4">
          </td>
        </tr>
        <tr>
          <td class="footer" align="center" height="15" valign="middle">
            Copyright &#169; 2001-2002, <a class="footer" href="./contact.html">Marco Hunsicker</a>. All rights reserved. Hosted by <a href="http://sourceforge.net">SourceForge.net</a>
          </td>
        </tr>
      </tbody>
    </table>
    <img src="http://sourceforge.net/sflogo.php?group_id=45216&amp;type=1" width="1" height="1" border="0" hspace="0" vspace="0" alt="" />
  </body>

  <!-- BODY ENDS HERE -->

</html>
</xsl:template>

<xsl:template name="article.titlepage" />

<xsl:template match="guibutton">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guiicon">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guilabel">
  <xsl:call-template name="inline.italicseq" />
</xsl:template>

<xsl:template match="guimenu">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guimenuitem">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guisubmenu">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

</xsl:stylesheet>