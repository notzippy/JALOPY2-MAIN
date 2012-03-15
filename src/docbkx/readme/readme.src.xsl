<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns:xhtml="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default xsl xhtml" >

<!-- load the main docbook stylesheet -->
<xsl:import href="@DIR.DOCBOOK.XSL@/html/profile-docbook.xsl" />

<xsl:param name="html.stylesheet">./docs/site.css</xsl:param>
<xsl:param name="css.decoration">1</xsl:param>
<xsl:param name="section.autolabel" select="1"/>
<xsl:param name="generate.toc" select="0"/>

<xsl:template match="*" mode="process.root">
  <xsl:variable name="doc" select="self::*"/>
  <html>
  <head>
    <xsl:call-template name="head.content">
      <xsl:with-param name="node" select="$doc"/>
    </xsl:call-template>
    <xsl:call-template name="user.head.content">
      <xsl:with-param name="node" select="$doc"/>
    </xsl:call-template>
  </head>
  <body id="toppage">
    <xsl:call-template name="body.attributes"/>
    <xsl:call-template name="user.header.content">
      <xsl:with-param name="node" select="$doc"/>
    </xsl:call-template>

    <table width="700" border="0" cellpadding="0" cellspacing="0" align="center">
      <tr>
        <td>
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
                    <a href="./docs/index.html" class="navlink">Overview</a> &#149;
                    <a href="./docs/download.html" class="navlink">Download</a> &#149;
                    <a href="./docs/docs.html" class="navlink">Documentation</a> &#149;
                    <a href="./docs/plugins.html" class="navlink">Plug-ins</a> &#149;
                    <a href="./docs/links.html" class="navlink">Links</a> &#149;
                    <a href="./docs/contact.html" class="navlink">Contact</a>
                  </td>
                </tr>
                <tr>
                  <td height="1" bgcolor="#ffffff">
                  </td>
                </tr>
              </tbody>
            </table>
        </td>
        </tr>
        <tr>
          <td height="20" bgcolor="#ffffff">
          </td>
        </tr>
        <tr>
          <td bgcolor="#eeeecc" height="17" style="padding-right:3px">
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
            Copyright &#169; 2001-2002, <a class="footer" href="./docs/contact.html">Marco Hunsicker</a>. All rights reserved.
          </td>
        </tr>
    </table>
    <xsl:call-template name="user.footer.content">
      <xsl:with-param name="node" select="$doc"/>
    </xsl:call-template>
  </body>
  </html>
</xsl:template>

<xsl:template name="article.titlepage" />

</xsl:stylesheet>