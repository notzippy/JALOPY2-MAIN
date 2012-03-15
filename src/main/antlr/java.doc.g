header
{
package de.hunsicker.jalopy.language.antlr;
}

{
import antlr.CommonAST;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import de.hunsicker.util.Lcs;
import java.io.FileInputStream;
import java.io.File;
import java.util.Collection;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
}

/**
 * Parser for Javadoc comments.
 *
 * <p><strong>Sample Usage:</strong></p>
 * <pre>
 * <blockquote style="background:lightgrey">
 *  // an input source
 *  Reader in = new BufferedReader(new FileReader(new File(argv[0])));
 *
 *  // create a lexer
 *  Lexer lexer = new JavadocLexer();
 *
 *  // set up the lexer to read from the input source
 *  lexer.setInputBuffer(in);
 *
 *  // get the corresponding parser
 *  Parser parser = lexer.getParser();
 *
 *  // and start the parsing process
 *  parser.parse();
 * </blockquote>
 * </pre>
 *
 * <p>This is an <a href="http://www.antlr.org">ANTLR</a> automated generated
 * file. <strong>DO NOT EDIT</strong> but rather change the associated grammar
 * (<code>java.doc.g</code>) and rebuild.</p>
 *
 * @version 1.0
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 *
 * @see de.hunsicker.jalopy.language.JavadocLexer
 * @see de.hunsicker.jalopy.language.Recognizer
 */
class InternalJavadocParser extends Parser;
options {
    genHashLines = true;
    importVocab = Common;
    exportVocab = Javadoc;
    k = 1;
    buildAST = true;
//    classHeaderSuffix = "Parser";
    defaultErrorHandler = true;
    codeGenMakeSwitchThreshold = 2;
    codeGenBitsetTestThreshold = 3;
//    useTokenPrefix = true;
	classHeaderPrefix = "public abstract";
}

tokens {
    OTH; CTH; OTD; CTD; TAG_CUSTOM; TAG_AUTHOR; TAG_DEPRECATED; TAG_EXCEPTION;
    TAG_THROWS; TAG_PARAM; TAG_RETURN; TAG_SEE; TAG_SINCE; TAG_SERIAL;
    TAG_SERIAL_DATA; TAG_SERIAL_FIELD; TAG_VERSION; TAG_INLINE_CUSTOM;
    TAG_INLINE_DOCROOT; TAG_INLINE_INHERITDOC; TAG_INLINE_LINK;
    TAG_INLINE_LINKPLAIN; TAG_INLINE_VALUE; TAG_TODO;
}
{
	/**
	 * Abstracted out to heading
     * @param ex
     */
    protected abstract void handleRecoverableError(RecognitionException ex) ;
    protected abstract void setTagType(AST tag, String type);

    /** Indicates an inline Javadoc tag. */
    protected final static String TYPE_INLINE = "TAG_INLINE_";

    /** Indicates a standard Javadoc tag. */
    protected final static String TYPE_STANDARD = "TAG_";
    
    /**
     * To satisfy antlr
     * 
     * @param ex
     * @param set
     */
	public void recover(RecognitionException ex, BitSet set) {
        
    }

}

internalParse
    :  {
            // Uncomment to check the tag definition files for update
            //loadTagInfo(false);
            //_startLine = _lexer.getLine();
            //_startColumn = _lexer.getColumn();
       }

      JAVADOC_OPEN! 
      	( body_content ) *
        ( standard_tag ) *
      	( body_content ) *
         JAVADOC_CLOSE!
    ;

body_content
    :   body_tag  | text | AT | TAG_OR_AT
    ;

body_tag
    :   block | address | heading
    ;

heading
    :   h1 | h2 | h3 | h4 | h5 | h6
    ;

block
    :   paragraph | list | preformatted | div |
        center | blockquote | HR | table
    ;

font:   teletype | italic | bold | underline | strike |
        big | small | subscript | superscript
    ;

phrase
    :   emphasize | strong | definition | code | sample |
        keyboard | variable | citation | acronym
    ;

special
    :   anchor | IMG | font_dfn | BR | typedclass
    ;

text_tag
    :   font | phrase | special
    ;

text
    :   ( LCURLY TAG ) => inline_tag | LCURLY | RCURLY | text_tag | COMMENT | PCDATA
    ;

/* BLOCK ELEMENTS */

h1
    :   OH1^ heading_content CH1
    ;

h2
    :   OH2^ heading_content CH2
    ;

h3
    :   OH3^ heading_content CH3
    ;

h4
    :   OH4^ heading_content CH4
    ;

h5
    :   OH5^ heading_content CH5
    ;

h6
    :   OH6^ heading_content CH6
    ;

heading_content
    :   (block | text)*
    ;

address
    :   OADDRESS (PCDATA)* CADDRESS
    ;

// NOTE:  according to the standard, paragraphs can't contain block elements
// like HR.  Netscape may insert these elements into paragraphs.
// We adhere strictly here.

paragraph
    :   OPARA^
        (
            /*  Rule body_content may also be just plain text because HTML is
                so loose.  When parse puts body_content in a loop, ANTLR
                doesn't know whether you want it to match all the text as part
                of this paragraph (in the case where the </p> is missing) or
                if the body rule should scarf it.  This is analogous to the
                dangling-else clause.  I shut off the warning.
            */
            options {
                generateAmbigWarnings=false;
            }
        :   text | list | div | center | blockquote | table | preformatted
        )*
        (CPARA)?
    ;
    exception // for rule
    catch [RecognitionException ex]
    {
        handleRecoverableError(ex);
    }


list
    :   unordered_list
    |   ordered_list
    |   def_list
    ;

unordered_list
    :   OULIST^ (PCDATA)* (list_item)+ CULIST
    ;

ordered_list
    :   OOLIST^ (PCDATA)* (list_item)+ COLIST
    ;

def_list
    :   ODLIST^ (PCDATA)* (def_list_item)+ CDLIST
    ;

list_item
    :   OLITEM^ ( text | block )+ (CLITEM (PCDATA)*)?
    ;

def_list_item
    :   dt | dd
    ;

dt
    :   ODTERM^ (text)+ (CDTERM (PCDATA)*)?
    ;

dd
    :   ODDEF^ (text | block)+ (CDDEF (PCDATA)*)?
    ;

dir
    :   ODIR^ (list_item)+ (CDIR)?
    ;

div
    :   ODIV^ (body_content)* CDIV     //semi-revised
    ;

center
    :   OCENTER^ (body_content)* CCENTER //semi-revised
    ;

blockquote
    :   OBQUOTE^ (body_content)* CBQUOTE
    ;

preformatted
    : PRE^
    ;

table
    :   OTABLE^ (caption)? (PCDATA)* (tr)+ CTABLE
    ;

caption
    :   OCAP^ (text)* CCAP
    ;

tr
    :   O_TR^ ((PCDATA)* | COMMENT) (th_or_td)* (C_TR ((PCDATA)* | COMMENT))?
    ;

th_or_td
    :   (OTH^ | OTD^) (body_content)* ((CTH | CTD) (PCDATA)*)?
    ;

/* TEXT ELEMENTS */

/* font style */

teletype
    :   OTTYPE^ ( text )+ CTTYPE
    ;

italic
    :   OITALIC^ ( text )+ CITALIC
    ;

bold:   OBOLD^ ( text )+ CBOLD
    ;

code:   OCODE^ ( text )+ CCODE
    ;

underline
    :   OUNDER^ ( text )+ CUNDER
    ;

strike
    :   OSTRIKE^ ( text )+ CSTRIKE
    ;

big :   OBIG^ ( text )+ CBIG
    ;

small
    :   OSMALL^ ( text )+ CSMALL
    ;

subscript
    :   OSUB^ ( text )+ CSUB
    ;

superscript
    :   OSUP^ ( text )+ CSUP
    ;

/* phrase elements */

emphasize
    :   OEM^ ( text )+ CEM
    ;

strong
    :   OSTRONG^ ( text )+ CSTRONG
    ;

definition
    :   ODFN^ ( text )+ CDFN
    ;

sample
    :   OSAMP^ ( text )+ CSAMP
    ;

keyboard
    :   OKBD^ ( text )+ CKBD
    ;

variable
    :   OVAR^ ( text )+ CVAR
    ;

citation
    :   OCITE^ ( text )+ CCITE
    ;

acronym
    :   OACRO^ ( text )+ CACRO
    ;

typedclass 
  : TYPEDCLASS^
  ;

/* special text level elements */
anchor
    :   OANCHOR^ ( anchor_content )*  CANCHOR
    ;

anchor_content
    :   font | phrase | PCDATA
    ;

//not w3-no blocks allowed; www.microsoft.com uses
font_dfn
    :   OFONT^ ( text )* CFONT
    ;

standard_tag
   :    (
            tag:TAG^ { setTagType(#tag, TYPE_STANDARD); } (text | block)*
//          | AT {#standard_tag.setType(JavadocTokenTypes.AT); }
        )

   ;

inline_tag
    :   LCURLY! tag:TAG^ { setTagType(#tag, TYPE_INLINE); }
        (~RCURLY)* RCURLY!
    ;


////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////

{
import de.hunsicker.util.Lcs;
import java.io.StringReader;
import de.hunsicker.io.FileFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
}

/**
 * Token lexer for the Javadoc parser.
 *
 * <p>This lexer has limited build-in error recovery which relies on the
 * generated token types mapping table (<code>JavadocTokenTypes.txt</code>).
 * Therefore it is a <strong>necessity to copy this file after every build into
 * </strong> the directory where the classfile comes to reside.</p>
 *
 * <p>I strongly encourage you to automate this process as part of your
 * <a href="http://jakarta.apache.org/ant/">Ant</a> build script or whatever
 * build tool you use.</p>
 *
 * <p><strong>Sample Usage:</strong></p>
 * <pre>
 * <blockquote style="background:lightgrey">
 *  // an input source
 *  Reader in = new BufferedReader(new FileReader(new File(argv[0])));
 *
 *  // create a lexer
 *  Lexer lexer = new JavadocLexer();
 *
 *  // set up the lexer to read from the input source
 *  lexer.setInputBuffer(in);
 *
 *  // get the corresponding parser
 *  Parser parser = lexer.getParser();
 *
 *  // and start the parsing process
 *  parser.parse();
 * </blockquote>
 * </pre>
 *
 * <p>This is an <a href="http://www.antlr.org">ANTLR</a> automated generated
 * file. <strong>DO NOT EDIT</strong> but rather change the associated grammar
 * (<code>java.doc.g</code>) and rebuild.</p>
 *
 * @version 1.0
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 *
 * @see de.hunsicker.jalopy.language.JavadocParser
 * @see de.hunsicker.jalopy.language.Recognizer
 */
class InternalJavadocLexer extends Lexer;
options {
    k = 4;
    genHashLines = true;
    exportVocab = Javadoc;
    charVocabulary = '\u0003'..'\uFFFE';
    codeGenMakeSwitchThreshold = 2;
    codeGenBitsetTestThreshold = 3;
    caseSensitive = false;
//    classHeaderSuffix = "Lexer";
    defaultErrorHandler = true;
//    useTokenPrefix = true;
	classHeaderPrefix = "public abstract";
}

{
    /** The detected file format. */
    protected FileFormat _fileFormat = FileFormat.UNKNOWN;

    /**
     * Replaces the tab char last read into the text buffer with an
     * equivalent number of spaces. Note that we assume you know what you do,
     * we don't check if indeed the tab char were read!
     *
     * @throws CharStreamException if an I/O error occured.
     */
     protected abstract void replaceTab() throws CharStreamException;

    /**
     * Skips leading spaces and asterix.
     *
     * @param skipAllLeadingWhitespace if <code>true</code>, all leading
     *        whitespace until the last space before the first word will be
     *        removed; if <code>false</code> only whitespace until and
     *        inclusive a leading asterix will be removed.
     * @throws CharStreamException if an I/O error occured.
     *
     * @see #newline(boolean)
     */
    protected abstract void skipLeadingSpaceAndAsterix(boolean skipAllLeadingWhitespace)
        throws CharStreamException;

    /**
     * Inserts a newline.
     *
     * @param skipAllLeadingWhitespace if <code>true</code>, all leading
     *        whitespace until the last space before the first word will be
     *        removed; if <code>false</code> only whitespace until and
     *        inclusive a leading asterix will be removed.
     *
     * @see #makeToken
     */
    public abstract void newline(boolean skipAllLeadingWhitespace);

    /**
     * Replaces the newline chars last read into the text buffer with a
     * single space. Note that we assume you know what you do; we don't check
     * if indeed newline chars were read!
     *
     * @param length length of the newline chars (1 or 2).
     * @throws CharStreamException if an I/O error occured.
     */
    protected abstract void replaceNewline(int length) throws CharStreamException;

    /**
     * To satisfy antlr
     * 
     * @param ex
     * @param set
     */
	public void recover(RecognitionException ex, BitSet set) {
        
    }

}

/* headings */
OH1
    :   "<h1" (WS ATTR)? '>'
    ;

CH1
    :   "</h1>"
    ;

OH2
    :   "<h2" (WS ATTR)?'>'
    ;

CH2
    :   "</h2>"
    ;

OH3
    :   "<h3" (WS ATTR)? '>'
    ;

CH3
    :   "</h3>"
    ;

OH4
    :   "<h4" (WS ATTR)? '>'
    ;

CH4
    :   "</h4>"
    ;

OH5
    :   "<h5" (WS ATTR)? '>'
    ;

CH5
    :   "</h5>"
    ;

OH6
    :   "<h6" (WS ATTR)? '>'
    ;

CH6
    :   "</h6>"
    ;

/*  STRUCTURAL tags */
OADDRESS
    :   "<address" (WS ATTR)? '>' (NEWLINE!)?
    ;

CADDRESS
    :   "</address>"
    ;

OPARA
    :   "<p" (WS ATTR)? '>' (NEWLINE!)?
    ;

CPARA
    :   "</p>"  //it's optional
    ;

/*UNORDERED LIST*/
OULIST
    :   "<ul" (WS ATTR)? '>' (NEWLINE!)?
    ;

CULIST
    :   "</ul>"
    ;

        /*ORDERED LIST*/
OOLIST
    :   "<ol" (WS ATTR)? '>' (NEWLINE!)?
    ;

COLIST
    :   "</ol>"
    ;

        /*LIST ITEM*/

OLITEM
    :   "<li" (WS ATTR)? '>' (NEWLINE!)?
    ;

CLITEM
    :   "</li>"
    ;

        /*DEFINITION LIST*/

ODLIST
    :   "<dl" (WS ATTR)? '>' (NEWLINE!)?
    ;

CDLIST
    :   "</dl>"
    ;

ODTERM
    :   "<dt"  (WS ATTR)? '>' (NEWLINE!)?
    ;

CDTERM
    :   "</dt>"
    ;

ODDEF
    :   "<dd" (WS ATTR)? '>' (NEWLINE!)?
    ;

CDDEF
    :   "</dd>"
    ;

ODIR:   "<dir" (WS ATTR)? '>' (NEWLINE!)?
    ;

CDIR_OR_CDIV
    :   "</di"
        (   'r' {$setType(JavadocTokenTypes.CDIR);}
        |   'v' {$setType(JavadocTokenTypes.CDIV);}
        )
        '>'
    ;

ODIV
    :   "<div" (WS ATTR)? '>' (NEWLINE!)?
    ;

OCENTER
    :   "<center" (WS ATTR)? '>' (NEWLINE!)?
    ;

CCENTER
    :   "</center>"
    ;

OBQUOTE
    :   "<blockquote" (WS ATTR)? '>' (NEWLINE!)?
    ;

CBQUOTE
    :   "</blockquote>"
    ;

//this is block element and thus can't be nested inside of
//other block elements, ex: paragraphs.
//Netscape appears to generate bad HTML vis-a-vis the standard.

HR
    :   "<hr" (WS (ATTR)*)? '>'
    ;


OTABLE
    :   "<table" (WS (ATTR)*)? '>' (NEWLINE!)?
    ;

CTABLE
    :   "</table>"
    ;

OCAP
    :   "<caption" (WS (ATTR)*)? '>' (NEWLINE!)?
    ;

CCAP
    :   "</caption>"
    ;

O_TR
    :   "<tr" (WS (ATTR)*)? '>' (NEWLINE!)?
    ;

C_TR
    :   "</tr>"
    ;

O_TH_OR_TD
    :   ("<th" {$setType(JavadocTokenTypes.OTH);}
        | "<td") {$setType(JavadocTokenTypes.OTD);}
        (WS (ATTR)*)? '>' (NEWLINE!)?
    ;

C_TH_OR_TD
    :
      "</th>" {$setType(JavadocTokenTypes.CTH);}
    | "</td>" {$setType(JavadocTokenTypes.CTD);}
    ;

/* PCDATA-LEVEL ELEMENTS */
/* font style elemens*/
OTTYPE
    :   "<tt" (WS ATTR)? '>' (NEWLINE!)?
    ;

CTTYPE
    :   "</tt>"
    ;

OCODE
    : "<code" (WS ATTR)? '>' (NEWLINE!)?
    ;

CCODE
    : "</code>"
    ;

OITALIC
    :   "<i" (WS ATTR)? '>' (NEWLINE!)?
    ;

CITALIC
    :   "</i>"
    ;

OBOLD
    :   "<b" (WS ATTR)? '>' (NEWLINE!)?
    ;

CBOLD
    :   "</b>"
    ;

OUNDER
    :   "<u" (WS ATTR)? '>' (NEWLINE!)?
    ;

CUNDER
    :   "</u>"
    ;

// Left-factor <strike></strike> and <strong></strong> to reduce lookahead
OSTRIKE_OR_OSTRONG
    :   (
            "<str"
            (   "ike" {$setType(JavadocTokenTypes.OSTRIKE);}
            |   "ong" {$setType(JavadocTokenTypes.OSTRONG);}
            )
            | "<s" {$setType(JavadocTokenTypes.OSTRIKE); $setText("<strike");}
        )
        (WS ATTR)? '>' (NEWLINE!)?
    ;

CSTRIKE_OR_CSTRONG
    :   (
            "</st"
            (   "rike" {$setType(JavadocTokenTypes.CSTRIKE);}
            |   "rong" {$setType(JavadocTokenTypes.CSTRONG);}
            )
            | "</s" {$setType(JavadocTokenTypes.CSTRIKE); $setText("</strike");}
        )
        '>'
    ;

OBIG
    :   "<big" (WS ATTR)? '>' (NEWLINE!)?
    ;

CBIG
    :   "</big>"
    ;

OSMALL
    :   "<small" (WS ATTR)? '>' (NEWLINE!)?
    ;

CSMALL
    :   "</small>"
    ;

OSUB:   "<sub" (WS ATTR)? '>' (NEWLINE!)?
    ;

OSUP
    :   "<sup" (WS ATTR)? '>' (NEWLINE!)?
    ;

CSUB_OR_CSUP
    :   "</su"
        (   'b' {$setType(JavadocTokenTypes.CSUB);}
        |   'p' {$setType(JavadocTokenTypes.CSUP);}
        )
        '>'
    ;

/*      phrase elements*/
OEM
    :   "<em" (WS ATTR)? '>' (NEWLINE!)?
    ;

CEM
    :   "</em>"
    ;

ODFN
    :   "<dfn" (WS ATTR)? '>' (NEWLINE!)?
    ;

CDFN
    :   "</dfn>"
    ;

OSAMP
    :   "<samp" (WS ATTR)? '>' (NEWLINE!)?
    ;

CSAMP
    :   "</samp>"
    ;

OKBD
    :   "<kbd" (WS ATTR)? '>' (NEWLINE!)?
    ;

CKBD
    :   "</kbd>"
    ;

OVAR
    :   "<var" (WS ATTR)? '>' (NEWLINE!)?
    ;

CVAR
    :   "</var>"
    ;

OCITE
    :   "<cite" (WS ATTR)? '>' (NEWLINE!)?
    ;

CCITE
    :   "</cite>"
    ;

OACRO
    :   "<acronym" (WS ATTR)? '>' (NEWLINE!)?
    ;

CACRO
    :   "</acronym>"
    ;

/* special text level elements*/
OANCHOR
    :   "<a" WS (ATTR)+ '>' (NEWLINE!)?
    ;

CANCHOR
    :   "</a>"
    ;

IMG
    :   "<img" WS (ATTR)+ '>'
    ;

OFONT
    :   "<font" WS (ATTR)+ '>' (NEWLINE!)?
    ;

CFONT
    :   "</font>"
    ;


BR
    :   "<br" (WS ATTR)? ('/')? '>'
    ;

STAR
    :   '*' { $setType(Token.SKIP); }
    ;

AT
    :   '@'
    ;

JAVADOC_OPEN
    :   "/**" (NEWLINE! { skipLeadingSpaceAndAsterix(true); })?
    ;

JAVADOC_CLOSE
    :   "*/"
    ;

RCURLY
    :   '}'
    ;

LCURLY
    :   '{'
    ;

PRE
    :   "<pre" (WS ATTR)? '>'
        (
            options {
                generateAmbigWarnings = false;
                greedy = false;
            }
        :   "\r\n" {newline(false);} // Evil DOS
        |   '\r'   {newline(false);} // Macintosh
        |   '\n'   {newline(false);} // Unix (the right way)
        |   .
        )*

        "</pre>"
    ;
    
TYPEDCLASS
    :   "<" LCLETTER (DIGIT)? '>'
    ;


/* MISC STUFF */
PCDATA
    :   (
            /* See comment in WS.  Language for combining any flavor
             * newline is ambiguous.  Shutting off the warning.
             */
            options {
                generateAmbigWarnings = false;
            }
        :

        '\r''\n'
                  {
                     // we remove the newline chars because we will calculate
                     // newlines at printing time
                     replaceNewline(2);

                     if (_fileFormat == FileFormat.UNKNOWN)
                        _fileFormat = FileFormat.DOS;
                  }
        |   '\r'
                  {  // we remove the newline chars because we will calculate
                     // newlines at printing time
                     replaceNewline(1);

                     if (_fileFormat == FileFormat.UNKNOWN)
                        _fileFormat = FileFormat.MAC;
                  }
        |   '\n'
                  { // we remove the newline chars because we will calculate
                    // newlines at printing time
                    replaceNewline(1);

                    if (_fileFormat == FileFormat.UNKNOWN)
                        _fileFormat = FileFormat.UNIX;
                  }
        | '\t'    {  replaceTab();
                  }
        | { LA(2) != '*' || (LA(2) == '*' && LA(3) != '*') }? '/' // allow slash
        | LCLETTER '@' {// Allow email address
                       }
        | ~('*'|'<'|'{'|'@'|'}'|'\n'|'\r'|'/') 
        )  +

        {
            String t = $getText;

            if (t != null)
            {
                t = t.trim();

                // remove trailing delimeter
                // XXX is this necessary?
                if (t.endsWith("*/"))
                {
                    t = t.substring(0, t.length() - 2).trim();
                }

                // skip empty nodes
                if (t.length() == 0)
                {
                    _token = null;
                    _createToken = false;
                }
            }
        }
    ;
    
TAG
    :   
        AT (LCLETTER)+ 
    ;


// multiple-line comments
protected
COMMENT_DATA
    :   (       /* '\r' '\n' can be matched in one alternative or by matching
                           '\r' in one iteration and '\n' in another.  I am trying to
                           handle any flavor of newline that comes in, but the language
                           that allows both "\r\n" and "\r" and "\n" to all be valid
                           newline is ambiguous.  Consequently, the resulting grammar
                           must be ambiguous.  I'm shutting this warning off.
                         */
                        options {
                                generateAmbigWarnings=false;
                        }
                :
                        {!(LA(2)=='-' && LA(3)=='>')}? '-' // allow '-' if not "-->"
                |       "\r\n"                  {newline();}
                |       '\r'                    {newline();}
                |       '\n'                    {newline();}
                |       ~('-'|'\n'|'\r')
                )*
    ;


COMMENT
    :   "<!--" c:COMMENT_DATA "-->" (WS)?
    ;

/*
 * PROTECTED LEXER RULES
 */
protected
WS
    :   (
            /*  '\r' '\n' can be matched in one alternative or by matching
                '\r' in one iteration and '\n' in another.  I am trying to
                handle any flavor of newline that comes in, but the language
                that allows both "\r\n" and "\r" and "\n" to all be valid
                newline is ambiguous.  Consequently, the resulting grammar
                must be ambiguous.  I'm shutting this warning off.
             */
            options {
                generateAmbigWarnings=false;
            }
        :   ' '
        |   '\t'   { replaceTab(); } // replace tab with space
        |   "\r\n"! { newline();} // Evil DOS
        |   '\r'!   { newline();} // Macintosh
        |   '\n'!   { newline();} // Unix (the right way)
        )+
    ;

protected
NEWLINE
    :   (
            /*  '\r' '\n' can be matched in one alternative or by matching
                '\r' in one iteration and '\n' in another.  I am trying to
                handle any flavor of newline that comes in, but the language
                that allows both "\r\n" and "\r" and "\n" to all be valid
                newline is ambiguous.  Consequently, the resulting grammar
                must be ambiguous.  I'm shutting this warning off.
             */
            options {
                generateAmbigWarnings=false;
            }
        :
            "\r\n"! {newline();} // Evil DOS
        |   '\r'!   {newline();} // Macintosh
        |   '\n'!   {newline();} // Unix (the right way)
        )
        { $setType(Token.SKIP); }
    ;

protected
ATTR
    :   // TODO: -INT% is in fact not allowed
        WORD (WS)? ('=' (WS)? (('-')? INT ('%')? | STRING | HEXNUM | WORD) (WS)?)?
    ;

// don't need uppercase for case-insen.
// the '.' is for words like "image.gif"
protected
WORD
    :   (   LCLETTER
        |   '.'
        )

        (
            /*  In reality, a WORD must be followed by whitespace, '=', or
                what can follow an ATTR such as '>'.  In writing this grammar,
                however, we just list all the possibilities as optional
                elements.  This is loose, allowing the case where nothing is
                matched after a WORD and then the (ATTR)* loop means the
                grammar would allow "widthheight" as WORD WORD or WORD, hence,
                an ambiguity.  Naturally, ANTLR will consume the input as soon
                as possible, combing "widthheight" into one WORD.

                I am shutting off the ambiguity here because ANTLR does the
                right thing.  The exit path is ambiguous with ever
                alternative.  The only solution would be to write an unnatural
                grammar (lots of extra productions) that laid out the
                possibilities explicitly, preventing the bogus WORD followed
                immediately by WORD without whitespace etc...
             */
            options {
                generateAmbigWarnings=false;
            }
        :   LCLETTER
        |   DIGIT
        |   '.'
        |   ':'
        |   '/'
        |   '@'
        )+
    ;

protected
STRING
    :   '"' (~'"')* '"'
    |   '\'' (~'\'')* '\''
    ;

protected
SPECIAL
    :   '<' | '~'
    ;

protected
HEXNUM
    :   '#' HEXINT
    ;

protected
INT :   (DIGIT)+
    ;

protected
HEXINT
    :   (
            /*  Technically, HEXINT cannot be followed by a..f, but due to our
                loose grammar, the whitespace that normally would follow this
                rule is optional.  ANTLR reports that #4FACE could parse as
                HEXINT "#4" followed by WORD "FACE", which is clearly bogus.
                ANTLR does the right thing by consuming a much input as
                possible here.  I shut the warning off.
             */
             options {
                generateAmbigWarnings=false;
            }
        :   HEXDIGIT
        )+
    ;

protected
DIGIT
    :   '0'..'9'
    ;

protected
HEXDIGIT
    :   '0'..'9'
    |   'a'..'f'
    ;

protected 
EMAILSTART
    :   '0'..'9'
    |   'a'..'z' | '\u00DF' .. '\u00FF'
    |   '_'
    ;
protected
LCLETTER
    :   'a'..'z' | '\u00DF' .. '\u00FF'
    ;   