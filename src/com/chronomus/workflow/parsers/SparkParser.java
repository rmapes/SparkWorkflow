/* Generated By:JavaCC: Do not edit this line. SparkParser.java */
package com.chronomus.workflow.parsers;

import java.io.*;
import java.util.*;
import com.chronomus.workflow.definition.*;
import com.chronomus.workflow.persistence.ServiceDbAccessor;
import com.chronomus.workflow.execution.Assignment;
import com.chronomus.workflow.execution.Task;
import com.chronomus.workflow.execution.MethodCall;
import com.chronomus.workflow.execution.expressions.*;
import com.chronomus.workflow.execution.expressions.primitives.*;

/**
 * Grammar to parse Spark
 * @author Richard Mapes
 */
public final class SparkParser implements SparkParserConstants {

    private static SparkParser parser;

    public static Workflow parse(ServiceDbAccessor dbAccessor, Reader in) throws ParseException {
        if (parser == null) {
            parser = new SparkParser (in);
        } else {
            parser.reset(in);
        }
        return parser.CompilationUnit(dbAccessor);
    }

    public static Workflow parse(ServiceDbAccessor dbAccessor, File file) throws ParseException {
        try {
                FileReader in = new FileReader(file);
                try {
                        return parse(dbAccessor, in);
                } finally {
                        in.close();
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reset(Reader in) {
        ReInit(in);
        token_source.clearComments();
    }

    List<Comment> getComments() {
        return token_source.getComments();
    }

/*****************************************
 * THE SPARK LANGUAGE GRAMMAR STARTS HERE *
 *****************************************/

/*
 * Program structuring syntax follows.
 */
  final public Workflow CompilationUnit(ServiceDbAccessor dbAccessor) throws ParseException {
  Workflow workflow = null;
    jj_consume_token(WORKFLOW);
    jj_consume_token(LBRACE);
    workflow = WorkflowDefinition(dbAccessor);
    jj_consume_token(RBRACE);
    switch (jj_nt.kind) {
    case 0:
      jj_consume_token(0);
      break;
    case 87:
      jj_consume_token(87);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return workflow;}
    throw new Error("Missing return statement in function");
  }

  final public Workflow WorkflowDefinition(ServiceDbAccessor dbAccessor) throws ParseException {
        Workflow workflow = new Workflow(dbAccessor);
    ConfigStatements(workflow);
    Definitions(workflow);
    {if (true) return workflow;}
    throw new Error("Missing return statement in function");
  }

  final public void ConfigStatements(Workflow workflow) throws ParseException {
        Task statement = null;
    label_1:
    while (true) {
      switch (jj_nt.kind) {
      case IDENTIFIER:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      statement = ConfigStatement(workflow);
                                           workflow.addTask(statement);
      jj_consume_token(SEMICOLON);
    }
  }

  final public Task ConfigStatement(Workflow workflow) throws ParseException {
        Task statement = null;
    if (jj_2_1(2)) {
      statement = VariableAssignment(workflow);
                                              {if (true) return statement;}
    } else {
      switch (jj_nt.kind) {
      case IDENTIFIER:
        statement = MethodCall(workflow);
                                      {if (true) return statement;}
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public Assignment VariableAssignment(Workflow workflow) throws ParseException {
        String name = null;
        Expression expression = null;
    jj_consume_token(IDENTIFIER);
                name = token.image;
    jj_consume_token(ASSIGN);
    expression = Expression(workflow);
    {if (true) return new Assignment(name, expression);}
    throw new Error("Missing return statement in function");
  }

  final public MethodCall MethodCall(Workflow workflow) throws ParseException {
        String name;
        List<Expression> parameters = new ArrayList<Expression>();
        Expression expr = null;
    jj_consume_token(IDENTIFIER);
                 name = token.image;
    jj_consume_token(LPAREN);
    switch (jj_nt.kind) {
    case NUMBER_LITERAL:
    case STRING_LITERAL:
    case IDENTIFIER:
    case LPAREN:
    case LBRACKET:
    case STAR:
      expr = Expression(workflow);
                                  parameters.add(expr);
      label_2:
      while (true) {
        switch (jj_nt.kind) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[3] = jj_gen;
          break label_2;
        }
        jj_consume_token(COMMA);
        expr = Expression(workflow);
                                                                                               parameters.add(expr);
      }
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
        MethodCall methodCall =  new MethodCall(name, workflow);
        methodCall.setParameters(parameters);
        {if (true) return methodCall;}
    throw new Error("Missing return statement in function");
  }

  final public void Definitions(Workflow workflow) throws ParseException {
        Definition def = null;
    label_3:
    while (true) {
      switch (jj_nt.kind) {
      case DEFINE:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_3;
      }
      def = Definition(workflow);
                                       workflow.addDefinition(def);
    }
  }

  final public Definition Definition(Workflow workflow) throws ParseException {
    String name;
        Job job = null;
        List<String> inputs = new ArrayList<String>();
    jj_consume_token(DEFINE);
    jj_consume_token(JOB);
    jj_consume_token(IDENTIFIER);
                                      name = token.image; job = new Job(name);
    jj_consume_token(LPAREN);
    switch (jj_nt.kind) {
    case IDENTIFIER:
      jj_consume_token(IDENTIFIER);
                         inputs.add(token.image);
      label_4:
      while (true) {
        switch (jj_nt.kind) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[6] = jj_gen;
          break label_4;
        }
        jj_consume_token(COMMA);
        jj_consume_token(IDENTIFIER);
                                                                       inputs.add(token.image);
      }
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
    switch (jj_nt.kind) {
    case COLON:
      jj_consume_token(COLON);
      JobConfiguration(job, workflow);
      break;
    default:
      jj_la1[8] = jj_gen;
      ;
    }
    jj_consume_token(LBRACE);
    JobDefinitionBody(job, workflow);
    jj_consume_token(RBRACE);
        job.setInputs(inputs);
        {if (true) return job;}
    throw new Error("Missing return statement in function");
  }

  final public void JobConfiguration(Job job, Workflow workflow) throws ParseException {
        List<Assignment> config = new ArrayList<Assignment>();
        Assignment ass;
    ass = VariableAssignment(workflow);
                                      config.add(ass);
    label_5:
    while (true) {
      switch (jj_nt.kind) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_5;
      }
      jj_consume_token(COMMA);
      ass = VariableAssignment(workflow);
                                                                                               config.add(ass);
    }
    job.setConfig(config);
  }

  final public void JobDefinitionBody(Job job, Workflow workflow) throws ParseException {
        Task task;
        Expression retVal;
        List<Expression> outs = new ArrayList<Expression>();
    label_6:
    while (true) {
      switch (jj_nt.kind) {
      case IDENTIFIER:
        ;
        break;
      default:
        jj_la1[10] = jj_gen;
        break label_6;
      }
      if (jj_2_2(2)) {
        task = VariableAssignment(workflow);
                                                 job.addTask(task);
      } else {
        switch (jj_nt.kind) {
        case IDENTIFIER:
          task = MethodCall(workflow);
                                         job.addTask(task);
          break;
        default:
          jj_la1[11] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      jj_consume_token(SEMICOLON);
    }
    switch (jj_nt.kind) {
    case RETURN:
      retVal = ReturnStatement(workflow);
                                               outs.add(retVal); job.setOutputs(outs);
      break;
    default:
      jj_la1[12] = jj_gen;
      ;
    }
  }

  final public Expression ReturnStatement(Workflow workflow) throws ParseException {
        Expression expr = null;
    jj_consume_token(RETURN);
    expr = Expression(workflow);
    jj_consume_token(SEMICOLON);
          {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public Expression Expression(Workflow workflow) throws ParseException {
        Expression expr = null;
    if (jj_2_3(2147483647)) {
      expr = ComplexExpression(workflow);
    } else {
      switch (jj_nt.kind) {
      case NUMBER_LITERAL:
      case STRING_LITERAL:
      case IDENTIFIER:
      case LBRACKET:
      case STAR:
        expr = SimpleExpression(workflow);
        break;
      default:
        jj_la1[13] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
          {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public Expression SimpleExpression(Workflow workflow) throws ParseException {
        Expression expr = null;
    switch (jj_nt.kind) {
    case STRING_LITERAL:
      jj_consume_token(STRING_LITERAL);
                           expr = new StringExpression(token.image.substring(1, token.image.length()-1));
      break;
    case NUMBER_LITERAL:
      jj_consume_token(NUMBER_LITERAL);
                           expr = new PrimitiveExpression(new NumberPrimitive(token.image));
      break;
    default:
      jj_la1[14] = jj_gen;
      if (jj_2_4(2147483647)) {
        expr = ParallelVariable(workflow);
      } else {
        switch (jj_nt.kind) {
        case LBRACKET:
          expr = ListExpression(workflow);
          break;
        default:
          jj_la1[15] = jj_gen;
          if (jj_2_5(2147483647)) {
            expr = MethodCall(workflow);
          } else {
            switch (jj_nt.kind) {
            case IDENTIFIER:
              jj_consume_token(IDENTIFIER);
                       expr = new Variable(token.image);
              break;
            default:
              jj_la1[16] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
          }
        }
      }
    }
          {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ComplexExpression(Workflow workflow) throws ParseException {
        Expression expr = null;
    if (jj_2_6(2147483647)) {
      expr = BinaryExpression(workflow);
    } else {
      switch (jj_nt.kind) {
      case LPAREN:
        expr = BracketedExpression(workflow);
        break;
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
         {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public Expression BracketedExpression(Workflow workflow) throws ParseException {
        Expression expr = null;
    jj_consume_token(LPAREN);
    expr = Expression(workflow);
    jj_consume_token(RPAREN);
         {if (true) return new BracketedExpression(expr);}
    throw new Error("Missing return statement in function");
  }

  final public Expression BinaryExpression(Workflow workflow) throws ParseException {
        Expression ret;
        Expression right;
        Operators.Binary op;
    switch (jj_nt.kind) {
    case LPAREN:
      ret = BracketedExpression(workflow);
      break;
    case NUMBER_LITERAL:
    case STRING_LITERAL:
    case IDENTIFIER:
    case LBRACKET:
    case STAR:
      ret = SimpleExpression(workflow);
      break;
    default:
      jj_la1[18] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch (jj_nt.kind) {
    case PLUS:
      jj_consume_token(PLUS);
                op = Operators.Binary.plus;
      break;
    case MINUS:
      jj_consume_token(MINUS);
                op = Operators.Binary.minus;
      break;
    case STAR:
      jj_consume_token(STAR);
                op = Operators.Binary.multiply;
      break;
    case SLASH:
      jj_consume_token(SLASH);
                op = Operators.Binary.divide;
      break;
    default:
      jj_la1[19] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    right = Expression(workflow);
                                         ret = new NumericBinaryExpression(ret, right, op);
    {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ParallelVariable(Workflow workflow) throws ParseException {
        Expression expr = null;
    jj_consume_token(STAR);
    expr = Expression(workflow);
          {if (true) return new ParallelVariable(expr);}
    throw new Error("Missing return statement in function");
  }

  final public Expression ListExpression(Workflow workflow) throws ParseException {
        Expression contents = null;
    jj_consume_token(LBRACKET);
    contents = RangeExpression(workflow);
    jj_consume_token(RBRACKET);
          {if (true) return new ListExpression(contents);}
    throw new Error("Missing return statement in function");
  }

  final public RangeExpression RangeExpression(Workflow workflow) throws ParseException {
        long iLeft;
        long iRight;
    jj_consume_token(INTEGER_LITERAL);
                            iLeft = Long.parseLong(token.image);
    jj_consume_token(MINUS);
    jj_consume_token(INTEGER_LITERAL);
                                                                                           iRight = Long.parseLong(token.image);
                {if (true) return new RangeExpression(iLeft, iRight);}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_3R_13() {
    if (jj_3R_22()) return true;
    return false;
  }

  private boolean jj_3R_9() {
    if (jj_scan_token(STAR)) return true;
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3R_8() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_12()) {
    jj_scanpos = xsp;
    if (jj_3R_13()) return true;
    }
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3R_12() {
    if (jj_3R_11()) return true;
    return false;
  }

  private boolean jj_3R_21() {
    if (jj_scan_token(SLASH)) return true;
    return false;
  }

  private boolean jj_3_5() {
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_20() {
    if (jj_scan_token(STAR)) return true;
    return false;
  }

  private boolean jj_3R_19() {
    if (jj_scan_token(MINUS)) return true;
    return false;
  }

  private boolean jj_3R_32() {
    if (jj_scan_token(IDENTIFIER)) return true;
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_scan_token(PLUS)) return true;
    return false;
  }

  private boolean jj_3R_25() {
    if (jj_scan_token(COMMA)) return true;
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3_4() {
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_30() {
    if (jj_3R_33()) return true;
    return false;
  }

  private boolean jj_3R_31() {
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_28() {
    if (jj_scan_token(NUMBER_LITERAL)) return true;
    return false;
  }

  private boolean jj_3R_17() {
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_3R_14()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_25()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_29() {
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_scan_token(STRING_LITERAL)) return true;
    return false;
  }

  private boolean jj_3R_26() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3R_28()) {
    jj_scanpos = xsp;
    if (jj_3R_29()) {
    jj_scanpos = xsp;
    if (jj_3R_30()) {
    jj_scanpos = xsp;
    if (jj_3R_31()) {
    jj_scanpos = xsp;
    if (jj_3R_32()) return true;
    }
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_3R_22()) return true;
    return false;
  }

  private boolean jj_3R_10() {
    if (jj_scan_token(IDENTIFIER)) return true;
    if (jj_scan_token(LPAREN)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_15()) jj_scanpos = xsp;
    if (jj_scan_token(RPAREN)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_16()) {
    jj_scanpos = xsp;
    if (jj_3R_17()) return true;
    }
    xsp = jj_scanpos;
    if (jj_3R_18()) {
    jj_scanpos = xsp;
    if (jj_3R_19()) {
    jj_scanpos = xsp;
    if (jj_3R_20()) {
    jj_scanpos = xsp;
    if (jj_3R_21()) return true;
    }
    }
    }
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3R_34() {
    if (jj_scan_token(INTEGER_LITERAL)) return true;
    if (jj_scan_token(MINUS)) return true;
    if (jj_scan_token(INTEGER_LITERAL)) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_3R_8()) return true;
    return false;
  }

  private boolean jj_3R_24() {
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_scan_token(LPAREN)) return true;
    if (jj_3R_14()) return true;
    if (jj_scan_token(RPAREN)) return true;
    return false;
  }

  private boolean jj_3R_7() {
    if (jj_scan_token(IDENTIFIER)) return true;
    if (jj_scan_token(ASSIGN)) return true;
    return false;
  }

  private boolean jj_3R_33() {
    if (jj_scan_token(LBRACKET)) return true;
    if (jj_3R_34()) return true;
    if (jj_scan_token(RBRACKET)) return true;
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_3R_8()) return true;
    return false;
  }

  private boolean jj_3R_14() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_23()) {
    jj_scanpos = xsp;
    if (jj_3R_24()) return true;
    }
    return false;
  }

  private boolean jj_3_6() {
    if (jj_3R_11()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public SparkParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[20];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
      jj_la1_init_2();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x1,0x0,0x0,0x0,0x2000000,0x800,0x0,0x0,0x0,0x0,0x0,0x0,0x4000,0x2000000,0x2000000,0x0,0x0,0x0,0x2000000,0x0,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x80,0x80,0x20000,0x4490,0x0,0x20000,0x80,0x2000000,0x20000,0x80,0x80,0x0,0x4090,0x10,0x4000,0x80,0x400,0x4490,0x0,};
   }
   private static void jj_la1_init_2() {
      jj_la1_2 = new int[] {0x800000,0x0,0x0,0x0,0x10,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x10,0x0,0x0,0x0,0x0,0x10,0x3c,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[6];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public SparkParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public SparkParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new SparkParserTokenManager(jj_input_stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public SparkParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new SparkParserTokenManager(jj_input_stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public SparkParser(SparkParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(SparkParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    token.next = jj_nt = token_source.getNextToken();
    jj_gen = 0;
    for (int i = 0; i < 20; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken = token;
    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;
    else jj_nt = jj_nt.next = token_source.getNextToken();
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    jj_nt = token;
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;
    else jj_nt = jj_nt.next = token_source.getNextToken();
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[88];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 20; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
          if ((jj_la1_2[i] & (1<<j)) != 0) {
            la1tokens[64+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 88; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 6; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
