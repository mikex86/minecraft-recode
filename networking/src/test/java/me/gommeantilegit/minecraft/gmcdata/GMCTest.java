package me.gommeantilegit.minecraft.gmcdata;

import com.badlogic.gdx.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GMCTest {

    @NotNull
    private static final byte[] SINGLE_PROPERTY_GMC_BYTES = new byte[]{0, 55, 43, 31, 0, 0, 0, 120, 0, 0, 0, 0, 0, 0, 0, 121, 0, 0, 0, 0, 0, 0, 0, 122, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1};

    @Test
    public void singlePropertyToGMC() throws IOException {
        SinglePropertyGMCBean bean = new SinglePropertyGMCBean(new Vector3(0, 0, 0));
        GMC gmc = new GMC();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gmc.toGMC(bos, bean);
        assertArrayEquals(bos.toByteArray(), SINGLE_PROPERTY_GMC_BYTES);
    }

    @Test
    public void singlePropertyFromGMC() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(SINGLE_PROPERTY_GMC_BYTES);
        GMC gmc = new GMC();
        SinglePropertyGMCBean bean = gmc.fromGMC(stream, SinglePropertyGMCBean.class);
        assertEquals(new SinglePropertyGMCBean(new Vector3(0, 0, 0)), bean);
    }

    @Test
    public void singlePropertyFromGMC_success_compatible() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(SINGLE_PROPERTY_GMC_BYTES);
        GMC gmc = new GMC();
        SinglePropertyCompatibleGMCBean bean = gmc.fromGMC(stream, SinglePropertyCompatibleGMCBean.class);
        assertEquals(new SinglePropertyCompatibleGMCBean(new Vector3(0, 0, 0)), bean);
    }

    @Test(expected = GMCCorruptedDataException.class)
    public void singlePropertyFromGMC_fail_random_bytes_end() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Arrays.copyOf(SINGLE_PROPERTY_GMC_BYTES, SINGLE_PROPERTY_GMC_BYTES.length + 1));
        GMC gmc = new GMC();
        SinglePropertyGMCBean bean = gmc.fromGMC(stream, SinglePropertyGMCBean.class);
        assertEquals(new SinglePropertyGMCBean(new Vector3(0, 0, 0)), bean);
    }

    @NotNull
    public static final byte[] MULTI_PROPERTY_BEAN_GMC_DATA = new byte[]{0, 1, -105, -17, 0, 0, 0, 4, 112, 38, -93, 22, 0, 0, 0, 120, 63, -128, 0, 0, 0, 0, 0, 121, 64, 0, 0, 0, 0, 0, 0, 122, 64, 64, 0, 0, -1, -1, -1, -1, -36, -69, -52, 95, 0, 1, -105, -17, 0, 0, 0, 17, -1, -1, -1, -1, -1, -1, -1, -1};

    @Test
    public void multiPropertyToGMC() throws IOException {
        MultiPropertyGMCBean bean = new MultiPropertyGMCBean(4, new Vector3(1, 2, 3), new SubBean(17));
        GMC gmc = new GMC();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gmc.toGMC(bos, bean);
        assertArrayEquals(bos.toByteArray(), MULTI_PROPERTY_BEAN_GMC_DATA);
    }

    @Test
    public void multiPropertyFromGMC() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(MULTI_PROPERTY_BEAN_GMC_DATA);
        MultiPropertyGMCBean bean = gmc.fromGMC(stream, MultiPropertyGMCBean.class);
        assertEquals(new MultiPropertyGMCBean(4, new Vector3(1, 2, 3), new SubBean(17)), bean);
    }

    @Test
    public void multiPropertyFromGMC_success_compatible() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(MULTI_PROPERTY_BEAN_GMC_DATA);
        CompatibleMultiPropertyCompatibleGMCBean bean = gmc.fromGMC(stream, CompatibleMultiPropertyCompatibleGMCBean.class);
        assertEquals(new CompatibleMultiPropertyCompatibleGMCBean(4, new Vector3(1, 2, 3), new SubBean(17)), bean);
    }

    @Test(expected = GMCCorruptedDataException.class)
    public void multiPropertyFromGMC_fail_random_bytes_end() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(Arrays.copyOf(MULTI_PROPERTY_BEAN_GMC_DATA, MULTI_PROPERTY_BEAN_GMC_DATA.length + 1));
        CompatibleMultiPropertyCompatibleGMCBean bean = gmc.fromGMC(stream, CompatibleMultiPropertyCompatibleGMCBean.class);
        assertEquals(new CompatibleMultiPropertyCompatibleGMCBean(4, new Vector3(1, 2, 3), new SubBean(17)), bean);
    }

    @Test
    public void morePropertiesFromGMC_success_stay_compatible() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(MULTI_PROPERTY_BEAN_GMC_DATA);
        gmc.fromGMC(stream, MorePropertiesMultiPropertyGMCBean.class, (propertyHash, beanType) -> {
            if (beanType.equals(MorePropertiesMultiPropertyGMCBean.class)) {
                if (propertyHash == "vector3f_2".hashCode()) {
                    return new Vector3(4, 3, 2);
                } else if (propertyHash == "int2".hashCode()) {
                    return 1;
                } else if (propertyHash == "vector3f_3".hashCode()) {
                    return new Vector3(7, 5, 3);
                }
            } else if (beanType.equals(MorePropertiesSubBean.class)) {
                if (propertyHash == "int2".hashCode()) {
                    return 27;
                }
            }
            return null;
        });
    }

    @Test(expected = GMCCorruptedDataException.class)
    public void morePropertiesFromGMC_fail_no_provided_defaults() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(MULTI_PROPERTY_BEAN_GMC_DATA);
        gmc.fromGMC(stream, MorePropertiesMultiPropertyGMCBean.class);
    }

    @NotNull
    private static final byte[] ARRAY_BEAN_GMC_DATA = {-24, -6, -67, 11, 0, 0, 0, 51, -71, 33, -15, -35, 60, -68, 4, -107, -3, -85, -116, -47, 77, 51, -16, -86, 64, -41, -47, 22, -110, -6, 38, 50, -101, -38, 87, 69, 66, 70, 15, 58, -50, 77, 118, -61, -97, 83, -100, -118, -32, 27, 13, 95, -116, -98, -44, -4, -73, 78, -12, -112, 41, -122, -9, -38, 18, 105, 39, 123, -82, -117, 71, -21, -53, -41, -62, 88, 68, -124, -15, 34, -108, 38, -53, 4, 62, 26, -47, 0, -24, 122, -21, -76, -102, 46, -54, 43, 38, 31, 68, 41, 55, 116, -59, -44, -7, -88, 2, -43, 117, 76, 41, -53, 16, -54, -26, -48, 22, -54, 104, -120, 17, -53, -20, -109, 31, 75, -8, 104, 23, 59, -118, -53, 86, -114, -127, -24, 108, -87, -39, -59, -18, -104, 0, -70, -33, -40, 23, 73, -83, -119, -7, -50, -109, 58, 1, -45, 0, 123, 88, -44, 101, -128, -47, -114, 127, -9, 17, -60, 66, 32, -14, -120, -5, 23, -41, 100, 39, 29, 98, -26, -51, 67, 37, -19, 66, -85, -119, 44, -56, -25, 25, 57, 118, 48, -55, -42, 3, -79, 24, 36, 112, 104, -24, -47, 60, 7, -37, 5, 54, 30, -86, -107, -1, -1, -1, -1};

    @Test
    public void toGMC_withArrays() throws IOException {
        GMC gmc = new GMC();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gmc.toGMC(bos, new ArrayWrappingBean());
        assertArrayEquals(bos.toByteArray(), ARRAY_BEAN_GMC_DATA);
    }

    @Test
    public void fromGMC_withArrays() throws IOException {
        GMC gmc = new GMC();
        ByteArrayInputStream stream = new ByteArrayInputStream(ARRAY_BEAN_GMC_DATA);
        ArrayWrappingBean bean = gmc.fromGMC(stream, ArrayWrappingBean.class);
        assertEquals(new ArrayWrappingBean(), bean);
    }

    public static class SinglePropertyGMCBean {

        @NotNull
        @SerializedProperty("vec3")
        private final Vector3 vector3;

        public SinglePropertyGMCBean(@NotNull Vector3 vector3) {
            this.vector3 = vector3;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SinglePropertyGMCBean gmcBean = (SinglePropertyGMCBean) o;
            return vector3.equals(gmcBean.vector3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vector3);
        }

        @NotNull
        public Vector3 getVector3() {
            return vector3;
        }
    }

    public static class SinglePropertyCompatibleGMCBean {

        @NotNull
        @SerializedProperty("vec3")
        private final Vector3 notVec3;

        public SinglePropertyCompatibleGMCBean(@NotNull Vector3 vector3) {
            this.notVec3 = vector3;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SinglePropertyCompatibleGMCBean gmcBean = (SinglePropertyCompatibleGMCBean) o;
            return notVec3.equals(gmcBean.notVec3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notVec3);
        }

        @NotNull
        public Vector3 getVector3() {
            return notVec3;
        }
    }

    public static class SubBean {

        @SerializedProperty("int")
        private final int theInteger;

        public SubBean(int theInteger) {
            this.theInteger = theInteger;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubBean subBean = (SubBean) o;
            return theInteger == subBean.theInteger;
        }

        @Override
        public int hashCode() {
            return Objects.hash(theInteger);
        }

        public int getTheInteger() {
            return theInteger;
        }
    }

    public static class MultiPropertyGMCBean {


        @SerializedProperty("int")
        private final int theTestInt;
        @NotNull
        @SerializedProperty("vector3f")
        private final Vector3 vector3;

        @NotNull
        @SerializedProperty("theSubBean")
        private final SubBean subBean;

        public MultiPropertyGMCBean(int theTestInt, @NotNull Vector3 vector3, @NotNull SubBean subBean) {
            this.theTestInt = theTestInt;
            this.vector3 = vector3;
            this.subBean = subBean;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MultiPropertyGMCBean that = (MultiPropertyGMCBean) o;
            return theTestInt == that.theTestInt &&
                    vector3.equals(that.vector3) &&
                    subBean.equals(that.subBean);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vector3, theTestInt, subBean);
        }

        @NotNull
        public Vector3 getVector3() {
            return vector3;
        }

        public int getTheTestInt() {
            return theTestInt;
        }
    }

    public static class CompatibleMultiPropertyCompatibleGMCBean {

        @SerializedProperty("int")
        private final int notTestIntInWrongOrder;

        @NotNull
        @SerializedProperty("vector3f")
        private final Vector3 notVector3;

        @NotNull
        @SerializedProperty("theSubBean")
        private final SubBean notSubBean;

        public CompatibleMultiPropertyCompatibleGMCBean(int notTestIntInWrongOrder, @NotNull Vector3 notVector3, @NotNull SubBean notSubBean) {
            this.notTestIntInWrongOrder = notTestIntInWrongOrder;
            this.notVector3 = notVector3;
            this.notSubBean = notSubBean;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompatibleMultiPropertyCompatibleGMCBean that = (CompatibleMultiPropertyCompatibleGMCBean) o;
            return notTestIntInWrongOrder == that.notTestIntInWrongOrder &&
                    notVector3.equals(that.notVector3) &&
                    notSubBean.equals(that.notSubBean);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notTestIntInWrongOrder, notVector3, notSubBean);
        }

        @NotNull
        public SubBean getNotSubBean() {
            return notSubBean;
        }

        @NotNull
        public Vector3 getNotVector3() {
            return notVector3;
        }

        public int getNotTestIntInWrongOrder() {
            return notTestIntInWrongOrder;
        }
    }


    public static class MorePropertiesSubBean {

        @SerializedProperty("int")
        private final int notTheInteger;

        @SerializedProperty("int2")
        private final int anAddedInteger;

        public MorePropertiesSubBean(int notTheInteger, int anAddedInteger) {
            this.notTheInteger = notTheInteger;
            this.anAddedInteger = anAddedInteger;
        }

        public int getAnAddedInteger() {
            return anAddedInteger;
        }

        public int getNotTheInteger() {
            return notTheInteger;
        }
    }

    public static class MorePropertiesMultiPropertyGMCBean {

        @SerializedProperty("int")
        private final int notTestIntInWrongOrder;

        @NotNull
        @SerializedProperty("vector3f")
        private final Vector3 notVector3;

        @NotNull
        @SerializedProperty("vector3f_2")
        private final Vector3 anotherVector3;

        @SerializedProperty("int2")
        private final int anotherAddedInteger;

        @NotNull
        @SerializedProperty("vector3f_3")
        private final Vector3 justAnotherVector3;

        @NotNull
        @SerializedProperty("theSubBean")
        private final MorePropertiesSubBean subBean;

        public MorePropertiesMultiPropertyGMCBean(int notTestIntInWrongOrder, @NotNull Vector3 notVector3, @NotNull Vector3 anotherVector3, int anotherAddedInteger, @NotNull Vector3 justAnotherVector3, @NotNull MorePropertiesSubBean subBean) {
            this.notTestIntInWrongOrder = notTestIntInWrongOrder;
            this.notVector3 = notVector3;
            this.anotherVector3 = anotherVector3;
            this.anotherAddedInteger = anotherAddedInteger;
            this.justAnotherVector3 = justAnotherVector3;
            this.subBean = subBean;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MorePropertiesMultiPropertyGMCBean that = (MorePropertiesMultiPropertyGMCBean) o;
            return notTestIntInWrongOrder == that.notTestIntInWrongOrder &&
                    anotherAddedInteger == that.anotherAddedInteger &&
                    notVector3.equals(that.notVector3) &&
                    anotherVector3.equals(that.anotherVector3) &&
                    justAnotherVector3.equals(that.justAnotherVector3) &&
                    subBean.equals(that.subBean);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notTestIntInWrongOrder, notVector3, anotherVector3, anotherAddedInteger, justAnotherVector3, subBean);
        }

        @NotNull
        public MorePropertiesSubBean getSubBean() {
            return subBean;
        }

        @NotNull
        public Vector3 getJustAnotherVector3() {
            return justAnotherVector3;
        }

        @NotNull
        public Vector3 getAnotherVector3() {
            return anotherVector3;
        }

        public int getAnotherAddedInteger() {
            return anotherAddedInteger;
        }

        @NotNull
        public Vector3 getNotVector3() {
            return notVector3;
        }

        public int getNotTestIntInWrongOrder() {
            return notTestIntInWrongOrder;
        }
    }

    public static class ArrayWrappingBean {

        @NotNull
        @SerializedProperty("the_array")
        private final int[] array;

        public ArrayWrappingBean() {
            this.array = new int[51];
            Random random = new Random(123);
            for (int i = 0; i < this.array.length; i++) {
                this.array[i] = random.nextInt();
            }
        }

        public ArrayWrappingBean(@NotNull int[] array) {
            this.array = array;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArrayWrappingBean that = (ArrayWrappingBean) o;
            return Arrays.equals(array, that.array);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }

        @NotNull
        public int[] getArray() {
            return array;
        }
    }
}