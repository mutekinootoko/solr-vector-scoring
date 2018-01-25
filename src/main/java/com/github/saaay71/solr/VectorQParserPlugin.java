package com.github.saaay71.solr;

import java.util.Iterator;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

public class VectorQParserPlugin extends QParserPlugin {
    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new QParser(qstr, localParams, params, req) {
            @Override
            public Query parse() throws SyntaxError {
                String field = localParams.get(QueryParsing.F);
                String vector = localParams.get("vector");
                boolean cosine = localParams.getBool("cosine", true);

                if (field == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'f' not specified");
                }

                if (vector == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "vector missing");
                }

                Query subQuery = subQuery(localParams.get(QueryParsing.V), null).getQuery();

                FieldType ft = req.getCore().getLatestSchema().getFieldType(field);
                if (ft != null) {
                    VectorQuery q = new VectorQuery(subQuery);
                    q.setQueryString(SolrParamsToLocalParamsString(localParams));
                    query = q;
                }

                if (query == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Query is null");
                }

                return new VectorScoreQuery(query, vector, field, cosine);

            }
            
            /**
             * copied and modified from solrj 6.6   
             * https://lucene.apache.org/solr/6_6_0/solr-solrj/org/apache/solr/common/params/SolrParams.html#toLocalParamsString--
             * Generates a local-params string of the form <pre>{! name=value name2=value2}</pre>.
             */
            public String SolrParamsToLocalParamsString(SolrParams mylocalParams) {
              final StringBuilder sb = new StringBuilder(128);
              sb.append("{!");
              //TODO perhaps look for 'type' and add here?  but it doesn't matter.
              for (final Iterator<String> it = mylocalParams.getParameterNamesIterator(); it.hasNext();) {
                final String name = it.next();
                for (String val : mylocalParams.getParams(name)) {
                  sb.append(' '); // do so even the first time; why not.
                  sb.append(name); // no escaping for name; it must follow "Java Identifier" rules.
                  sb.append('=');
                  sb.append(ClientUtilsEncodeLocalParamVal(val));
                }
              }
              sb.append('}');
              //System.out.println(sb.toString());
              return sb.toString();
            }
            
            /**
             * copied and modified from ClientUtils  in solrj 6.6 
             * https://lucene.apache.org/solr/6_6_0/solr-solrj/org/apache/solr/client/solrj/util/ClientUtils.html#encodeLocalParamVal-java.lang.String-
             * Returns the value encoded properly so it can be appended after a <pre>name=</pre> local-param.
             */
            public String ClientUtilsEncodeLocalParamVal(String val) {
              int len = val.length();
              int i = 0;
              if (len > 0 && val.charAt(0) != '$') {
                for (;i<len; i++) {
                  char ch = val.charAt(i);
                  if (Character.isWhitespace(ch) || ch=='}') break;
                }
              }

              if (i>=len) return val;

              // We need to enclose in quotes... but now we need to escape
              StringBuilder sb = new StringBuilder(val.length() + 4);
              sb.append('\'');
              for (i=0; i<len; i++) {
                char ch = val.charAt(i);
                if (ch=='\'') {
                  sb.append('\\');
                }
                sb.append(ch);
              }
              sb.append('\'');
              return sb.toString();
            }
        };
    }

}
