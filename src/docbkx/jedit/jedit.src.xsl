<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns:xhtml="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default xsl xhtml" >

<!-- load the main docbook stylesheet -->
<xsl:import href="@DIR.DOCBOOK.XSL@/html/docbook.xsl" />

<!-- override templates to customize -->

<!-- Swing HTML control doesn't support &ldquo; and &rdquo; -->
<xsl:template match="quote">&quot;<xsl:apply-templates/>&quot;</xsl:template>

<xsl:template match="guibutton">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guiicon">
  <xsl:call-template name="inline.boldseq" />
</xsl:template>

<xsl:template match="guilabel">
  <xsl:call-template name="inline.boldseq" />
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