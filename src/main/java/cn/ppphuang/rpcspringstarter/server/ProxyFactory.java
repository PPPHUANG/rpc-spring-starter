package cn.ppphuang.rpcspringstarter.server;

import cn.ppphuang.rpcspringstarter.common.constants.RpcConstant;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务代理工厂
 *
 * @Author: ppphuang
 * @Create: 2021/11/23
 */
@Slf4j
public class ProxyFactory {

    //生成代理类
    public static Object makeProxy(String interfaceName, String springBeanName, Method[] methods) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.appendSystemPath();
        CtClass proxyClass = pool.makeClass(interfaceName + "$proxy" + System.currentTimeMillis());
        //实现invoke接口
        CtClass invokeProxy = pool.get(RpcConstant.INVOKE_PROXY_INTERFACE_NAME);
        proxyClass.addInterface(invokeProxy);
        //生成存储被代理类的属性
        String proxyFiledString = makeProxyFiled(interfaceName, springBeanName);
        CtField proxyField = CtField.make(proxyFiledString, proxyClass);
        proxyClass.addField(proxyField);
        //创建接口方法
        String exceptionName = Exception.class.getName();
        String objectName = Object.class.getName();
        //获取唯一方法名列表 可能存在方法重载
        ArrayList<String> uniqueMethodNameList = new ArrayList<>();
        ArrayList<Method> uniqueMethodList = new ArrayList<>();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (!uniqueMethodNameList.contains(methodName)) {
                uniqueMethodNameList.add(methodName);
                uniqueMethodList.add(methods[i]);
            }
        }
        //按唯一方法名生成方法
        for (Method uniqueMethod : uniqueMethodList) {
            String uniqueMethodName = uniqueMethod.getName();
            if ("equals".equalsIgnoreCase(uniqueMethodName) || "toString".equalsIgnoreCase(uniqueMethodName) || "hashCode".equalsIgnoreCase(uniqueMethodName)) {
                continue;
            }
            String methodString = makeMethodString(proxyClass, methods, uniqueMethodName);
            //生成方法
            CtMethod methodItem = CtMethod.make(methodString, proxyClass);
            //添加到代理类中
            proxyClass.addMethod(methodItem);
        }
        //创建invok方法
        String invokeMethodString = makeInvoke(uniqueMethodList, proxyClass, exceptionName, objectName);
        CtMethod invoke = CtMethod.make(invokeMethodString, proxyClass);
        proxyClass.addMethod(invoke);
        return proxyClass.toClass().newInstance();
    }

    private static String makeMethodString(CtClass proxyClass, Method[] methods, String uniqueMethodName) {
        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(RpcConstant.RPC_RESPONSE_CLASS_NAME).append(" ");
        sb.append(uniqueMethodName);
        sb.append("(" + RpcConstant.RPC_REQUEST_CLASS_NAME + " request) throws " + RpcConstant.EXCEPTION_CLASS_NAME + " {");
        sb.append(RpcConstant.OBJECT_CLASS_NAME + "[] params = " + "request.getParameters();");
        //这里循环所有方法列表 解决方法重载问题 重载的方法都生成到一个方法里 通过参数长度、类型来判定调用哪一个
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            if (!uniqueMethodName.equalsIgnoreCase(name)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            Type[] mGenericType = method.getGenericParameterTypes();
            sb.append("if(params.length == ").append(mGenericType.length);
            for (int j = 0; j < mGenericType.length; j++) {
                String paraName = getParaName(mGenericType[j], parameterTypes[j]);

                sb.append(" && (");
                sb.append("params[");
                sb.append(j);
                sb.append("] == null ||");
                sb.append("params[");
                sb.append(j);
                sb.append("].getClass().getSimpleName().equalsIgnoreCase(\"");
                sb.append(paraName);
                sb.append("\")");

                if (paraName.contains("int")) {
                    sb.append("|| params[");
                    sb.append(j);
                    sb.append("].getClass().getSimpleName().equalsIgnoreCase(\"" + paraName.replaceAll("int", "Integer") + "\")");
                } else if (paraName.contains("Integer")) {
                    sb.append("|| params[");
                    sb.append(j);
                    sb.append("].getClass().getSimpleName().equalsIgnoreCase(\"" + paraName.replaceAll("Integer", "int") + "\")");
                } else if (paraName.contains("char")) {
                    sb.append("|| params[");
                    sb.append(j);
                    sb.append("].getClass().getSimpleName().equalsIgnoreCase(\"" + paraName.replaceAll("char", "Character") + "\")");
                } else if (paraName.contains("Character")) {
                    sb.append("|| params[");
                    sb.append(j);
                    sb.append("].getClass().getSimpleName().equalsIgnoreCase(\"" + paraName.replaceAll("Character", "char") + "\")");
                }
                sb.append(")");
            }
            sb.append("){");
            for (int j = 0; j < mGenericType.length; j++) {
                String paraName = mGenericType[j].toString().replaceFirst("class ", "");
                if (paraName.startsWith("[")) {
                    paraName = parameterTypes[j].getCanonicalName();
                }

                sb.append(paraName.replaceAll("\\<.*\\>", ""));
                sb.append(" arg" + j);

                paraName = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");

                if (paraName.equals("long")) {
                    sb.append(" = 0L;");
                } else if (paraName.equals("float")) {
                    sb.append(" = 0F;");
                } else if (paraName.equals("double")) {
                    sb.append(" = 0D;");
                } else if (paraName.equals("int")) {
                    sb.append(" = 0;");
                } else if (paraName.equals("short")) {
                    sb.append(" = (short)0;");
                } else if (paraName.equals("byte")) {
                    sb.append(" = (byte)0;");
                } else if (paraName.equals("boolean")) {
                    sb.append(" = false;");
                } else if (paraName.equals("char")) {
                    sb.append(" = (char)'\\0';");
                } else if (paraName.equals("Long")) {
                    sb.append(" = new Long(\"0\");");
                } else if (paraName.equals("Float")) {
                    sb.append(" = new Float(\"0\");");
                } else if (paraName.equals("Double")) {
                    sb.append(" = new Double(\"0\");");
                } else if (paraName.equals("Integer")) {
                    sb.append(" = new Integer(\"0\");");
                } else if (paraName.equals("Short")) {
                    sb.append(" = new Short(\"0\");");
                } else if (paraName.equals("Byte")) {
                    sb.append(" = new Byte(\"0\");");
                } else if (paraName.equals("Boolean")) {
                    sb.append(" = new Boolean(\"false\");");
                } else if (paraName.equals("Character")) {
                    sb.append(" = new Character((char)'\\0');");
                } else {
                    sb.append(" = null;");
                }
            }

            for (int j = 0; j < mGenericType.length; j++) {
                String paraName = mGenericType[j].toString().replaceFirst("class ", "");

                if (paraName.startsWith("[")) {
                    paraName = parameterTypes[j].getCanonicalName();
                }

                String pn = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
                if (pn.equalsIgnoreCase("String")
                        || pn.equalsIgnoreCase("int") || pn.equalsIgnoreCase("Integer")
                        || pn.equalsIgnoreCase("long")
                        || pn.equalsIgnoreCase("short")
                        || pn.equalsIgnoreCase("float")
                        || pn.equalsIgnoreCase("boolean")
                        || pn.equalsIgnoreCase("double")
                        || pn.equalsIgnoreCase("char") || pn.equalsIgnoreCase("Character")
                        || pn.equalsIgnoreCase("byte")) {
                    sb.append("arg" + j);
                    sb.append(" = " + RpcConstant.CONVERT_UTIL_CLASS_NAME + ".convertTo" + pn + "(params[" + j + "]);");
                } else {
                    sb.append("arg" + j);
                    sb.append(" = (" + paraName.replaceAll("\\<.*\\>", "") + ")" + RpcConstant.CONVERT_UTIL_CLASS_NAME + ".convertToT(params[" + j + "]), \"" + paraName + "\");");
                }
            }

            //define returnValue
            Class<?> classReturn = method.getReturnType();
            Type typeReturn = method.getGenericReturnType();
            String returnValueType = typeReturn.getTypeName().replaceFirst("class ", "");
            if (returnValueType.startsWith("[")) {
                returnValueType = classReturn.getCanonicalName();
            }
            if (!returnValueType.equalsIgnoreCase("void")) {
                sb.append(returnValueType.replaceAll("\\<.*\\>", "") + " returnValue = ");
            }
            //通过设置的代理类属性调用方法
            sb.append("serviceProxy.");
            sb.append(method.getName());
            sb.append("(");
            //method para
            for (int j = 0; j < mGenericType.length; j++) {
                sb.append("arg");
                sb.append(j);
                if (j != mGenericType.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(");");
            if (!"void".equalsIgnoreCase(returnValueType)) {
                sb.append("return new ").append(RpcConstant.RPC_RESPONSE_CLASS_NAME).append("(returnValue);");
            } else {
                sb.append("return null;");
            }
            sb.append("}");
        }

        sb.append("throw new ").append(RpcConstant.EXCEPTION_CLASS_NAME).append("(\"method:").append(proxyClass.getName()).append(".").append(uniqueMethodName).append("--msg:not fond method error\");");
        sb.append("}");
        log.debug("method({}) source code:{}", proxyClass.getName() + uniqueMethodName, sb);
        return sb.toString();
    }

    private static String makeProxyFiled(String interfaceName, String springBeanName) {
        //使用springcontext获取bean
        return "private static " +
                interfaceName +
                " serviceProxy = " +
                "((org.springframework.context.ApplicationContext) " + RpcConstant.CONTAINER_CLASS_NAME + ".getSpringContext()).getBean("
                + "\"" + springBeanName +
                "\");";
    }

    private static String makeInvoke(ArrayList<Method> methods, CtClass proxyClass, String exceptionName, String objectName) {
        StringBuilder sb = new StringBuilder();
        sb.append("public " + RpcConstant.RPC_RESPONSE_CLASS_NAME + " invoke(" + RpcConstant.RPC_REQUEST_CLASS_NAME + " request) throws " + exceptionName + " {");
        sb.append("String methodName = request.getMethod();");
        for (Method method : methods) {
            String methodName = method.getName();
            if ("equals".equalsIgnoreCase(methodName) || "toString".equalsIgnoreCase(methodName) || "hashCode".equalsIgnoreCase(methodName)) {
                continue;
            }
            sb.append("if(methodName.equalsIgnoreCase(\"");
            sb.append(methodName);
            sb.append("\")){");
            sb.append(objectName + " returnValue = ");
            sb.append(methodName);
            sb.append("(request);");
            sb.append("return returnValue;");
            sb.append("}");
        }
        sb.append("throw new ").append(exceptionName).append("(\"method:").append(proxyClass.getName()).append(".invoke--msg:not found method (\"+methodName+\")\");");
        sb.append("}");
        log.debug("{}:invoke source code:{}", proxyClass.getName(), sb);
        return sb.toString();
    }

    private static String getSimpleParaName(Type type, Class<?> clazz) {
        String paraName = type.toString().replaceFirst("class ", "");
        paraName = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
        if (paraName.startsWith("[")) {
            paraName = clazz.getCanonicalName();
        }
        paraName = getSimpleParaName(paraName);
        return paraName;
    }


    private static String getParaName(Type type, Class<?> clazz) {
        String paraName = getSimpleParaName(type, clazz);

        if (paraName.contains("<[") || paraName.contains(";>")) {
            paraName = paraName.replaceAll("\\[L", "").replaceAll("\\[", "");
        }
        //兼容jdk6 jdk7
        if (paraName.contains("[]>") || paraName.contains("[],")) {
            paraName = paraName.replace("[]>", ";>").replace("[],", ";,");
        }
        return paraName;
    }

    public static String getSimpleParaName(String paraName) {
        paraName = paraName.replaceAll(" ", "");
        if (paraName.indexOf(".") > 0) {
            String[] pnAry = paraName.split("");
            List<String> originalityList = new ArrayList<String>();
            List<String> replaceList = new ArrayList<String>();

            String tempP = "";
            for (int i = 0; i < pnAry.length; i++) {
                if (pnAry[i].equalsIgnoreCase("<")) {
                    originalityList.add(tempP);
                    replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
                    tempP = "";
                } else if (pnAry[i].equalsIgnoreCase(">")) {
                    originalityList.add(tempP);
                    replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
                    tempP = "";
                } else if (pnAry[i].equalsIgnoreCase(",")) {
                    originalityList.add(tempP);
                    replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
                    tempP = "";
                } else if (i == pnAry.length - 1) {
                    originalityList.add(tempP);
                    replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
                    tempP = "";
                } else {
                    if (!pnAry[i].equalsIgnoreCase("[") && !pnAry[i].equalsIgnoreCase("]")) {
                        tempP += pnAry[i];
                    }
                }
            }

            for (int i = 0; i < replaceList.size(); i++) {
                paraName = paraName.replaceAll(originalityList.get(i), replaceList.get(i));
            }
            return paraName;
        } else {
            return paraName;
        }
    }

}
