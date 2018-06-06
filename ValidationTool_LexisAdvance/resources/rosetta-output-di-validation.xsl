<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:docinfo="http://www.lexis-nexis.com/glp/docinfo" xmlns:case="http://www.lexis-nexis.com/glp/case" xmlns:leg="http://www.lexis-nexis.com/glp/leg" xmlns:user="http://mynamespace1" xmlns:fn="http://mynamespace2" version="2.0">

<!-- 
        Developed By        :   Keshav Kumar and Sandeep Kumar
        Version             :   1.0
        Modification Date   :   12-Oct-2017
-->    

    <xsl:output method="text"/>

    <xsl:variable name="files1" select="collection(concat(replace(substring-before(document-uri(.), tokenize(document-uri(.), '/')[last()]), '%20', ' '), '?select=*.xml')) | collection(concat(replace(substring-before(document-uri(.), tokenize(document-uri(.), '/')[last()]), '%20', ' '), '?select=*.XML'))"/>
    
    <xsl:function name="fn:mulbookseqnum">
        <xsl:param name="file"/>
        <xsl:for-each select="$file">
            <xsl:if test=".//docinfo:bookseqnum[normalize-space(.)!=''][normalize-space(.)=$files1[document-uri(.)!=document-uri(current())]//docinfo:bookseqnum[normalize-space(.)!='']]">
                <xsl:value-of select="tokenize(document-uri(.), '/')[last()]"/>
                <xsl:text>&#x0a;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:function>

    <xsl:template match="/">
        <xsl:result-document href="DIValidationLog.txt" method="text">
            <xsl:text>Dated: </xsl:text>
            <xsl:value-of select="format-dateTime(current-dateTime(), '[Y0001]/[M01]/[D01] [H01]:[m01]:[s01]')"/>
            <xsl:text>&#x0a;&#x0a;</xsl:text>
                <xsl:choose>
                    <xsl:when test="$files1//fnr[not(@fntoken = //footnote/@fntoken)] or $files1//footnote[@fntoken = ./preceding::footnote/@fntoken] or $files1//remotelink[@refpt='' or @dpsi='' or not(@refpt) or not(@dpsi)] or $files1[not(.//docinfo:bookseqnum)] or $files1//docinfo:bookseqnum[normalize-space(.)=''] or $files1[contains(tokenize(document-uri(.), '/')[last()], '.XML')] or $files1//LEGDOC//leg:body//leg:level/leg:level-vrnt[1][@toc-caption=''] or $files1//LEGDOC//leg:body//leg:level/leg:level-vrnt[1][not(attribute::toc-caption)] or $files1//*[@href=''] or $files1//locator[translate(@anchoridref, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_:', '') != ''] or fn:mulbookseqnum($files1) or fn:dateValidation($files1//case:decisiondate/date)">
                        <xsl:for-each select="$files1">

                        <!-- 1. Missing footnote ID -->

                        <xsl:for-each select=".//fnr">
                            <xsl:choose>
                                <xsl:when test="./@fntoken = //footnote/@fntoken"/>
                                <xsl:otherwise>
                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                    <xsl:text>: </xsl:text>
                                    <xsl:text>Footnote reference with refID: </xsl:text><xsl:value-of select="./@fntoken"/><xsl:text> does not exist</xsl:text>
                                    <xsl:text>&#x0a;</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>

                        <!-- 2. Duplicate footnote ID -->

                        <xsl:for-each select=".//footnote[@fntoken = preceding::footnote/@fntoken]">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:text>Footnote with ID </xsl:text><xsl:value-of select="@fntoken"/><xsl:text> are duplicate</xsl:text>
                            <xsl:text>&#x0a;</xsl:text>
                        </xsl:for-each>

                        <!-- 3. Blank remotelink/@refpt or remotelink/@dpsi -->

                        <xsl:for-each select=".//remotelink[@refpt='' or @dpsi='' or not(@refpt) or not(@dpsi)]">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:choose>
                                <xsl:when test="not(@refpt) and @dpsi">
                                    <xsl:text>Missing @refpt attribute </xsl:text>
                                </xsl:when>
                                <xsl:when test="@refpt and not(@dpsi)">
                                    <xsl:text>Missing @dpsi attribute </xsl:text>
                                </xsl:when>
                                <xsl:when test="not(@refpt) and not(@dpsi)">
                                    <xsl:text>Missing both @refpt and @dpsi attribute </xsl:text>
                                </xsl:when>
                                <xsl:when test="normalize-space(@refpt)='' and normalize-space(@dpsi)!=''">
                                    <xsl:text>Blank value of @refpt </xsl:text>
                                </xsl:when>
                                <xsl:when test="normalize-space(@refpt)!='' and normalize-space(@dpsi)=''">
                                    <xsl:text>Blank value of @dpsi </xsl:text>
                                </xsl:when>
                                <xsl:when test="normalize-space(@refpt)='' and normalize-space(@dpsi)=''">
                                    <xsl:text>Blank value of both @refpt and @dpsi </xsl:text>
                                </xsl:when>
                            </xsl:choose>
                            <xsl:text>&lt;</xsl:text>
                            <xsl:value-of select="name(.)"/>
                            <xsl:for-each select="@*">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="name(.)"/>
                                <xsl:text>="</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>"</xsl:text>
                            </xsl:for-each>
                            <xsl:text>&gt;</xsl:text>
                            <xsl:text>&#x0a;</xsl:text>
                        </xsl:for-each>

                        <!-- 4. Blank docinfo:bookseqnum -->

                        <xsl:choose>
                            <xsl:when test=".[not(.//docinfo:bookseqnum)]">
                                <xsl:value-of select="tokenize(document-uri(.), '/')[last()]"/>
                                <xsl:text>: Does not contain bookseqnum&#x0a;</xsl:text>
                            </xsl:when>
                            <xsl:when test=".//docinfo:bookseqnum[normalize-space(.)='']">
                                <xsl:value-of select="tokenize(document-uri(.), '/')[last()]"/>
                                <xsl:text>: Blank bookseqnum&#x0a;</xsl:text>
                            </xsl:when>
                        </xsl:choose>

                        <!-- 5. Duplicate docinfo:bookseqnu -->

                        <xsl:for-each select=".//docinfo:bookseqnum[normalize-space(.)!=''][normalize-space(.)=$files1[document-uri(.)!=document-uri(current())]//docinfo:bookseqnum[normalize-space(.)!='']]">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: Have duplicate bookseqnum&#x0a;</xsl:text>
                        </xsl:for-each>

                        <!-- 6. Upper case File extension -->

                        <xsl:if test="contains(tokenize(document-uri(.), '/')[last()], '.XML')">
                            <xsl:value-of select="tokenize(document-uri(.), '/')[last()]"/>
                            <xsl:text>: File extension is in upper case&#x0a;</xsl:text>
                        </xsl:if>

                        <!-- 7. rosetta ID and docinfo:doc-id must not be equal  -->

                        <xsl:if test=".//rosetta/@id = .//docinfo:doc-id">
                            <xsl:value-of select="tokenize(document-uri(.), '/')[last()]"/>
                            <xsl:text>(</xsl:text>
                            <xsl:value-of select=".//rosetta/@id"/>
                            <xsl:text>) rosetta id and docinfo:doc-id value should not be equal&#x0a;</xsl:text>
                        </xsl:if>

                        <!-- 8. leg:level-vrnt/@toc-caption must not be empty or missing  -->

                        <xsl:for-each select=".//LEGDOC//leg:body//leg:level/leg:level-vrnt[1][@toc-caption=''] | .//LEGDOC//leg:body//leg:level/leg:level-vrnt[1][not(attribute::toc-caption)]">
                            <xsl:choose>
                                <xsl:when test="not(@toc-caption)">
                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                    <xsl:text>: </xsl:text>
                                    <xsl:text>toc-caption attribute of element leg:level-vrnt is not present</xsl:text>
                                    <xsl:text>&lt;leg:level-vrnt</xsl:text>
                                    <xsl:for-each select="@*">
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="name(.)"/>
                                        <xsl:text>="</xsl:text>
                                        <xsl:value-of select="."/>
                                        <xsl:text>"</xsl:text>
                                    </xsl:for-each>
                                    <xsl:text>&gt;&#x0a;</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                    <xsl:text>: </xsl:text>
                                    <xsl:text>toc-caption attribute of element leg:level-vrnt is blank</xsl:text>
                                    <xsl:text>: </xsl:text>
                                    <xsl:text>&lt;leg:level-vrnt</xsl:text>
                                    <xsl:for-each select="@*">
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="name(.)"/>
                                        <xsl:text>="</xsl:text>
                                        <xsl:value-of select="."/>
                                        <xsl:text>"</xsl:text>
                                    </xsl:for-each>
                                    <xsl:text>&gt;&#x0a;</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>

                        <!-- 9. @href must not be empty -->

                        <xsl:for-each select=".//*[@href='']">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:text>href attribute should not be empty: </xsl:text>
                            <xsl:text>&lt;</xsl:text>
                            <xsl:value-of select="name()"/>
                            <xsl:for-each select="@*">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="name(.)"/>
                                <xsl:text>="</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>"</xsl:text>
                            </xsl:for-each>
                            <xsl:text>&gt;&#x0a;</xsl:text>
                        </xsl:for-each>

                        <!-- 10. locator/@anchoridref value must be valid NMTOKEN  -->

                        <xsl:for-each select=".//locator[translate(@anchoridref, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_:', '') != '']">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:text>locator/@anchoridref value not valid NMTOKEN: </xsl:text>
                            <xsl:text>&lt;</xsl:text>
                            <xsl:value-of select="name()"/>
                            <xsl:for-each select="@*">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="name(.)"/>
                                <xsl:text>="</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>"</xsl:text>
                            </xsl:for-each>
                            <xsl:text>&gt;&#x0a;</xsl:text>
                        </xsl:for-each>

                        <xsl:for-each select=".//entry[@charoff]">
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:text>entry contains @charoff attribute: </xsl:text>
                            <xsl:text>&lt;</xsl:text>
                            <xsl:value-of select="name()"/>
                            <xsl:for-each select="@*">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="name(.)"/>
                                <xsl:text>="</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>"</xsl:text>
                            </xsl:for-each>
                            <xsl:text>&gt;&#x0a;</xsl:text>
                        </xsl:for-each>

                        <!-- 11. Validation on case:decisiondate/date -->

                        <xsl:value-of select="fn:dateValidation(.//case:decisiondate/date)"/>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>No Error</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
        </xsl:result-document>
    </xsl:template>

    <user:Date>
        <monthInfo max="31" umonth="1"/>
        <monthInfo max="31" umonth="3"/>
        <monthInfo max="30" umonth="4"/>
        <monthInfo max="31" umonth="5"/>
        <monthInfo max="30" umonth="6"/>
        <monthInfo max="31" umonth="7"/>
        <monthInfo max="31" umonth="8"/>
        <monthInfo max="30" umonth="9"/>
        <monthInfo max="31" umonth="10"/>
        <monthInfo max="30" umonth="11"/>
        <monthInfo max="31" umonth="12"/>
    </user:Date>

    <xsl:function name="fn:dateValidation">
        <xsl:param name="dateV"/>
        <xsl:for-each select="$dateV">
            <xsl:choose>
                <xsl:when test="string-length(@month)&lt;=2 and string-length(@month)&gt;=1">
                    <xsl:choose>
                        <xsl:when test="number(@month)&gt;=1 and number(@month)&lt;=12">
                            <xsl:choose>
                                <xsl:when test="string-length(@day)&lt;=2 and string-length(@day)&gt;=1">
                                    <xsl:choose>
                                        <xsl:when test="number(@month)!=2">
                                            <xsl:choose>
                                                <xsl:when test="number(@day)&lt;=number(document('')//user:Date/monthInfo[number(@umonth)=number(current()/@month)]/@max) and number(@day)&gt;=1"/>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                                    <xsl:text>: </xsl:text>
                                                    <xsl:text>&lt;</xsl:text>
                                                    <xsl:value-of select="name(.)"/>
                                                    <xsl:for-each select="@*">
                                                        <xsl:text> </xsl:text>
                                                        <xsl:value-of select="name(.)"/>
                                                        <xsl:text>="</xsl:text>
                                                        <xsl:value-of select="."/>
                                                        <xsl:text>"</xsl:text>
                                                    </xsl:for-each>
                                                    <xsl:text>&gt; </xsl:text>
                                                    <xsl:text>@day = </xsl:text>
                                                    <xsl:value-of select="@day"/>
                                                    <xsl:text>: Wrong value of day attribute&#x0a;</xsl:text>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <!-- Test for february -->
                                            <xsl:choose>
                                                <xsl:when test="number(@year) mod 4 = 0">
                                                    <xsl:choose>
                                                        <xsl:when test="number(@year) mod 100 = 0">
                                                            <xsl:choose>
                                                                <xsl:when test="number(@year) mod 400 = 0">
                                                                    <xsl:choose>
                                                                        <xsl:when test="number(@day) &lt;= 29 and number(@day) &gt;= 1"/>
                                                                        <xsl:otherwise>
                                                                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                                                            <xsl:text>: </xsl:text>
                                                                            <xsl:text>&lt;</xsl:text>
                                                                            <xsl:value-of select="name(.)"/>
                                                                            <xsl:for-each select="@*">
                                                                                <xsl:text> </xsl:text>
                                                                                <xsl:value-of select="name(.)"/>
                                                                                <xsl:text>="</xsl:text>
                                                                                <xsl:value-of select="."/>
                                                                                <xsl:text>"</xsl:text>
                                                                            </xsl:for-each>
                                                                            <xsl:text>&gt; </xsl:text>
                                                                            <xsl:text>@day = </xsl:text>
                                                                            <xsl:value-of select="@day"/>
                                                                            <xsl:text>: Wrong value of day attribute&#x0a;</xsl:text>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:choose>
                                                                        <xsl:when test="number(@day) &lt;= 28 and number(@day) &gt;= 1"/>
                                                                        <xsl:otherwise>
                                                                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                                                            <xsl:text>: </xsl:text>
                                                                            <xsl:text>&lt;</xsl:text>
                                                                            <xsl:value-of select="name(.)"/>
                                                                            <xsl:for-each select="@*">
                                                                                <xsl:text> </xsl:text>
                                                                                <xsl:value-of select="name(.)"/>
                                                                                <xsl:text>="</xsl:text>
                                                                                <xsl:value-of select="."/>
                                                                                <xsl:text>"</xsl:text>
                                                                            </xsl:for-each>
                                                                            <xsl:text>&gt; </xsl:text>
                                                                            <xsl:text>@day = </xsl:text>
                                                                            <xsl:value-of select="@day"/>
                                                                            <xsl:text>: Wrong value of day attribute&#x0a;</xsl:text>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:choose>
                                                                <xsl:when test="number(@day) &lt;= 29 and number(@day) &gt;= 1"/>
                                                                <xsl:otherwise>
                                                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                                                    <xsl:text>: </xsl:text>
                                                                    <xsl:text>&lt;</xsl:text>
                                                                    <xsl:value-of select="name(.)"/>
                                                                    <xsl:for-each select="@*">
                                                                        <xsl:text> </xsl:text>
                                                                        <xsl:value-of select="name(.)"/>
                                                                        <xsl:text>="</xsl:text>
                                                                        <xsl:value-of select="."/>
                                                                        <xsl:text>"</xsl:text>
                                                                    </xsl:for-each>
                                                                    <xsl:text>&gt; </xsl:text>
                                                                    <xsl:text>@day = </xsl:text>
                                                                    <xsl:value-of select="@day"/>
                                                                    <xsl:text>: Wrong value of day attribute&#x0a;</xsl:text>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:choose>
                                                        <xsl:when test="number(@day) &lt;= 28 and number(@day) &gt;= 1"/>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                                            <xsl:text>: </xsl:text>
                                                            <xsl:text>&lt;</xsl:text>
                                                            <xsl:value-of select="name(.)"/>
                                                            <xsl:for-each select="@*">
                                                                <xsl:text> </xsl:text>
                                                                <xsl:value-of select="name(.)"/>
                                                                <xsl:text>="</xsl:text>
                                                                <xsl:value-of select="."/>
                                                                <xsl:text>"</xsl:text>
                                                            </xsl:for-each>
                                                            <xsl:text>&gt; </xsl:text>
                                                            <xsl:text>@day = </xsl:text>
                                                            <xsl:value-of select="@day"/>
                                                            <xsl:text>: Wrong value of day attribute&#x0a;</xsl:text>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                                    <xsl:text>: </xsl:text>
                                    <xsl:text>&lt;</xsl:text>
                                    <xsl:value-of select="name(.)"/>
                                    <xsl:for-each select="@*">
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="name(.)"/>
                                        <xsl:text>="</xsl:text>
                                        <xsl:value-of select="."/>
                                        <xsl:text>"</xsl:text>
                                    </xsl:for-each>
                                    <xsl:text>&gt; </xsl:text>
                                    <xsl:text>@day = </xsl:text>
                                    <xsl:value-of select="@day"/>
                                    <xsl:text>: is not in required format&#x0a;</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                            <xsl:text>: </xsl:text>
                            <xsl:text>&lt;</xsl:text>
                            <xsl:value-of select="name(.)"/>
                            <xsl:for-each select="@*">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="name(.)"/>
                                <xsl:text>="</xsl:text>
                                <xsl:value-of select="."/>
                                <xsl:text>"</xsl:text>
                            </xsl:for-each>
                            <xsl:text>&gt; </xsl:text>
                            <xsl:text>@month = </xsl:text>
                            <xsl:value-of select="@month"/>
                            <xsl:text>: Wrong value of month attribute&#x0a;</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="tokenize(document-uri(/), '/')[last()]"/>
                    <xsl:text>: </xsl:text>
                    <xsl:text>&lt;</xsl:text>
                    <xsl:value-of select="name(.)"/>
                    <xsl:for-each select="@*">
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="name(.)"/>
                        <xsl:text>="</xsl:text>
                        <xsl:value-of select="."/>
                        <xsl:text>"</xsl:text>
                    </xsl:for-each>
                    <xsl:text>&gt; </xsl:text>
                    <xsl:text>@month = </xsl:text>
                    <xsl:value-of select="@month"/>
                    <xsl:text>: is not in required format&#x0a;</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>

</xsl:stylesheet>