package org.jetbrains.addActivity;

import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;

/**
 * User: Java
 * Date: 1/14/13
 */
public class AddActivityAction extends AnAction {

    private PsiElementFactory factory;
    private String folderName = "MyTest";

    public void actionPerformed(AnActionEvent e) {
        if (e == null || e.getProject() == null) {
            return;
        }

        VirtualFile parent = e.getData(LangDataKeys.VIRTUAL_FILE);

        if (parent == null || !parent.isDirectory()) {
            return;
        }

        /*Folder create*/
        PsiDirectory dir = createFolder(e, parent, "myNewFolder");

        factory = new PsiElementFactoryImpl(PsiManager.getInstance(e.getProject()));

        /*============= *View Interface create ================*/

        PsiClass viewInterface = factory.createInterface(folderName + "View");

        /*import IsWidget;*/
        PsiImportStatement importStatement = factory.createImportStatementOnDemand("com.google.gwt.user.client.ui");

        viewInterface.add(importStatement);

        generateExtends(viewInterface, "com.google.gwt.user.client.ui.IsWidget", "IsWidget");

        dir.add(viewInterface);

        /*=====================================================*/

        /*============= *Place Class ==========================*/

//        PsiClass placeClass = factory.createClass(folderName + "Place");
//        generateExtends(placeClass, "com.google.gwt.place.shared.Place", "Place");
//
//        PsiClass placeTokenizer = factory.createClassFromText("" +
//                "import com.google.gwt.user.client.ui.IsWidget;\n" +
//                "import com.google.gwt.place.shared.PlaceTokenizer;\n\n" +
//                "public static class " + folderName + "PlaceTokenizer implements PlaceTokenizer<" + folderName + "Place> {\n" +
//                "\n" +
//                "        @Override\n" +
//                "        public " + folderName + "Place getPlace(String token) {\n" +
//                "            return new " + folderName + "Place();\n" +
//                "        }\n" +
//                "\n" +
//                "        @Override\n" +
//                "        public String getToken(" + folderName + "Place place) {\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "    }", null);
//
//        placeClass.add(placeTokenizer);

//        dir.add(placeClass);

//        placeClass.getFields()


        /*=====================================================*/

        /*============= *Place Class ==========================*/

//        dir.add(aClass);


    }

    private void generateImplementsComparable(PsiClass psiClass) {
        PsiClassType[] implementsListTypes = psiClass.getImplementsListTypes();
        for (PsiClassType implementsListType : implementsListTypes) {
            PsiClass resolved = implementsListType.resolve();
            if (resolved != null && "java.lang.Comparable".equals(resolved.getQualifiedName())) {
                return;
            }
        }

        String implementsType = "Comparable<" + psiClass.getName() + ">";
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiJavaCodeReferenceElement referenceElement = elementFactory.createReferenceFromText(implementsType, psiClass);
        psiClass.getImplementsList().add(referenceElement);
    }

    private void generateExtends(PsiClass psiClass, String fullPackagePath, String name) {
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();

        for (PsiClassType extendsListType : extendsListTypes) {
            PsiClass resolved = extendsListType.resolve();
            if (resolved != null && fullPackagePath.equals(resolved.getQualifiedName())) {
                return;
            }
        }

        addToReferences(psiClass, name);
    }

    private void addToReferences(PsiClass psiClass, String name) {
        PsiJavaCodeReferenceElement referenceElement = factory.createReferenceFromText(name, psiClass);
        psiClass.getExtendsList().add(referenceElement);
    }

    public PsiDirectory createFolder(AnActionEvent e, VirtualFile folder, final String name) {

        PsiManagerImpl psiManager = new PsiManagerImpl(e.getProject()
                , FileDocumentManager.getInstance()
                , PsiBuilderFactory.getInstance()
                , FileIndexFacade.getInstance(e.getProject())
                , getEventProject(e).getMessageBus()
                , new PsiModificationTrackerImpl(e.getProject())
        );

        final PsiDirectory parent = new PsiDirectoryImpl(psiManager, folder);

        final PsiDirectory[] result = {null};

        for (PsiDirectory dir : parent.getSubdirectories()) {
            if (dir.getName().equalsIgnoreCase(name)) {
                result[0] = dir;
                break;
            }
        }

        if (null == result[0]) {

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    result[0] = parent.createSubdirectory(name);
                }
            });

        }

        return result[0];
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

        e.getPresentation().setEnabled(file != null && file.isDirectory());
    }
}
