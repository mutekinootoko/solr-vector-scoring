package com.github.saaay71.solr;

import java.io.IOException;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ConstantScoreScorer;
import org.apache.lucene.search.ConstantScoreWeight;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

public class VectorQuery extends Query {
    String queryStr = "";
    Query q;

    public VectorQuery(Query subQuery) {
        this.q = subQuery;
    }

    public void setQueryString(String queryString) {
        this.queryStr = queryString;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
        Weight w;
        if (q == null) {
            w = new ConstantScoreWeight(this) {
                @Override
                public Scorer scorer(LeafReaderContext context) throws IOException {
                    return new ConstantScoreScorer(this, score(), DocIdSetIterator.all(context.reader().maxDoc()));
                }
            };
        } else {
            w = searcher.createWeight(q, needsScores);
        }
        return w;
    }

    @Override
    public String toString(String field) {
        return queryStr;
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) &&
                queryStr.equals(other.toString());
    }

    @Override
    public int hashCode() {
        return classHash() ^ queryStr.hashCode();
    }
    
    
    /**
     * copied from lucene 6.6 
     * https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/search/Query.html#sameClassAs-java.lang.Object-
     * 
     * Utility method to check whether <code>other</code> is not null and is exactly 
     * of the same class as this object's class.
     * 
     * When this method is used in an implementation of {@link #equals(Object)},
     * consider using {@link #classHash()} in the implementation
     * of {@link #hashCode} to differentiate different class
     */
    protected final boolean sameClassAs(Object other) {
      return other != null && getClass() == other.getClass();
    }

    private final int CLASS_NAME_HASH = getClass().getName().hashCode();

    /**
     *  copied from lucene 6.6
     *  https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/search/Query.html#classHash--
     * 
     * 
     * Provides a constant integer for a given class, derived from the name of the class.
     * The rationale for not using just {@link Class#hashCode()} is that classes may be
     * assigned different hash codes for each execution and we want hashes to be possibly
     * consistent to facilitate debugging.    
     */
    protected final int classHash() {
      return CLASS_NAME_HASH;
    }

}
