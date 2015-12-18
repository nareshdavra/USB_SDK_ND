//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.digitalpersona.uareu;

public class UareUException extends Exception {
    public static final int URU_E_SUCCESS = 0;
    public static final int URU_E_NOT_IMPLEMENTED = 96075786;
    public static final int URU_E_FAILURE = 96075787;
    public static final int URU_E_NO_DATA = 96075788;
    public static final int URU_E_MORE_DATA = 96075789;
    public static final int URU_E_INVALID_PARAMETER = 96075796;
    public static final int URU_E_INVALID_DEVICE = 96075797;
    public static final int URU_E_DEVICE_BUSY = 96075806;
    public static final int URU_E_DEVICE_FAILURE = 96075807;
    public static final int URU_E_INVALID_FID = 96075877;
    public static final int URU_E_TOO_SMALL_AREA = 96075878;
    public static final int URU_E_INVALID_FMD = 96075977;
    public static final int URU_E_ENROLLMENT_IN_PROGRESS = 96076077;
    public static final int URU_E_ENROLLMENT_NOT_STARTED = 96076078;
    public static final int URU_E_ENROLLMENT_NOT_READY = 96076079;
    public static final int DPFJ_E_ENROLLMENT_INVALID_SET = 96076080;
    private static final long serialVersionUID = 4311262755669951497L;
    private final String m_str;
    private final int m_code;

    public UareUException(int n) {
        switch(n) {
            case 0:
                this.m_code = n;
                this.m_str = "API call succeeded.";
                break;
            case 96075786:
                this.m_code = n;
                this.m_str = "API call is not implemented.";
                break;
            case 96075787:
                this.m_code = n;
                this.m_str = "Reason for the failure is unknown or cannot be specified.";
                break;
            case 96075788:
                this.m_code = n;
                this.m_str = "No data is available.";
                break;
            case 96075789:
                this.m_code = n;
                this.m_str = "The memory allocated by the application is not big enough for the data which is expected.";
                break;
            case 96075796:
                this.m_code = n;
                this.m_str = "One or more parameters passed to the API call are invalid.";
                break;
            case 96075797:
                this.m_code = n;
                this.m_str = "Reader handle is not valid.";
                break;
            case 96075806:
                this.m_code = n;
                this.m_str = "The API call cannot be completed because another call is in progress.";
                break;
            case 96075807:
                this.m_code = n;
                this.m_str = "The reader is not working properly.";
                break;
            case 96075877:
                this.m_code = n;
                this.m_str = "FID is invalid.";
                break;
            case 96075878:
                this.m_code = n;
                this.m_str = "Image is too small.";
                break;
            case 96075977:
                this.m_code = n;
                this.m_str = "FMD is invalid.";
                break;
            case 96076077:
                this.m_code = n;
                this.m_str = "Enrollment operation is in progress.";
                break;
            case 96076078:
                this.m_code = n;
                this.m_str = "Enrollment operation has not begun.";
                break;
            case 96076079:
                this.m_code = n;
                this.m_str = "Not enough in the pool of FMDs to create enrollment FMD.";
                break;
            case 96076080:
                this.m_code = n;
                this.m_str = "Unable to create enrollment FMD with the collected set of FMDs.";
                break;
            default:
                this.m_code = -1;
                this.m_str = String.format("Unknown error, code: 0x%x", new Object[]{Integer.valueOf(n)});
        }

    }

    public int getCode() {
        return this.m_code;
    }

    public String toString() {
        return this.m_str;
    }
}
