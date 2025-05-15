"use client"

import type React from "react"

import { useState, useRef, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { ArrowRight, RefreshCw } from "lucide-react"
import {verifyOtp} from "@/app/api/auth/index"

export default function VerifyOtpPage() {
    const router = useRouter()
    const searchParams = useSearchParams()
    const email = searchParams.get("email") || ""

    const [otp, setOtp] = useState(["", "", "", "", "", ""])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState("")
    const [resendDisabled, setResendDisabled] = useState(true)
    const [countdown, setCountdown] = useState(30)

    const inputRefs = useRef<(HTMLInputElement | null)[]>([])

    useEffect(() => {
        // Focus the first input on component mount
        if (inputRefs.current[0]) {
            inputRefs.current[0].focus()
        }

        // Start countdown for resend button
        const timer = setInterval(() => {
            setCountdown((prev) => {
                if (prev <= 1) {
                    clearInterval(timer)
                    setResendDisabled(false)
                    return 0
                }
                return prev - 1
            })
        }, 1000)

        return () => clearInterval(timer)
    }, [])

    const handleChange = (index: number, value: string) => {
        // Only allow numbers
        if (value && !/^\d+$/.test(value)) return

        const newOtp = [...otp]
        // Take only the last character if multiple characters are pasted
        newOtp[index] = value.slice(-1)
        setOtp(newOtp)

        // Auto-focus next input
        if (value && index < 5 && inputRefs.current[index + 1]) {
            inputRefs.current[index + 1].focus()
        }
    }

    const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
        // Navigate between inputs with arrow keys
        if (e.key === "ArrowRight" && index < 5) {
            inputRefs.current[index + 1]?.focus()
        } else if (e.key === "ArrowLeft" && index > 0) {
            inputRefs.current[index - 1]?.focus()
        } else if (e.key === "Backspace" && !otp[index] && index > 0) {
            // Move to previous input on backspace if current input is empty
            const newOtp = [...otp]
            newOtp[index - 1] = ""
            setOtp(newOtp)
            inputRefs.current[index - 1]?.focus()
        }
    }

    const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
        e.preventDefault()
        const pastedData = e.clipboardData.getData("text/plain").trim()

        // Check if pasted content is a valid OTP
        if (!/^\d+$/.test(pastedData)) return

        const digits = pastedData.split("").slice(0, 6)
        const newOtp = [...otp]

        digits.forEach((digit, index) => {
            if (index < 6) newOtp[index] = digit
        })

        setOtp(newOtp)

        // Focus the appropriate input after paste
        if (digits.length < 6 && inputRefs.current[digits.length]) {
            inputRefs.current[digits.length].focus()
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        setError("")

        const otpValue = otp.join("")

        if (otpValue.length !== 6) {
            setError("Please enter a valid 6-digit OTP")
            setLoading(false)
            return
        }

        try {
            const response = await verifyOtp({ otp: otpValue , action : 'verify'})

            if (!response.ok) {
                throw new Error(await response.text())
            } else {
                router.push("/auth/login")
            }
        } catch (err) {
            setError("Invalid OTP. Please try again.")
        } finally {
            setLoading(false)
        }
    }

    const handleResendOtp = async () => {
        setResendDisabled(true)
        setCountdown(30)

        try {
            const response = await verifyOtp({ otp: '', action: 'resend' })
            setError("")

            if (!response.ok) {
                throw new Error(await response.text())
            }

            // Start countdown again
            const timer = setInterval(() => {
                setCountdown((prev) => {
                    if (prev <= 1) {
                        clearInterval(timer)
                        setResendDisabled(false)
                        return 0
                    }
                    return prev - 1
                })
            }, 1000)
        } catch (err) {
            setError("Failed to resend OTP")
            setResendDisabled(false)
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8">
                <div>
                    <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">Verify your email</h2>
                    <p className="mt-2 text-center text-sm text-gray-600">
                        We've sent a verification code to{" "}
                        <span className="font-medium text-blue-600">{email || "your email address"}</span>
                    </p>
                </div>

                <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-400 p-4">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                        <path
                                            fillRule="evenodd"
                                            d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                                            clipRule="evenodd"
                                        />
                                    </svg>
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm text-red-700">{error}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    <div>
                        <label htmlFor="otp" className="block text-sm font-medium text-gray-700 mb-2">
                            Enter verification code
                        </label>
                        <div className="flex justify-between gap-2">
                            {otp.map((digit, index) => (
                                <input
                                    key={index}
                                    ref={(el) => (inputRefs.current[index] = el)}
                                    type="text"
                                    maxLength={1}
                                    value={digit}
                                    onChange={(e) => handleChange(index, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(index, e)}
                                    onPaste={index === 0 ? handlePaste : undefined}
                                    className="w-12 h-12 text-center text-xl font-semibold border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                                    required
                                />
                            ))}
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={loading}
                            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
              <span className="absolute left-0 inset-y-0 flex items-center pl-3">
                <ArrowRight className="h-5 w-5 text-blue-500 group-hover:text-blue-400" />
              </span>
                            {loading ? "Verifying..." : "Verify"}
                        </button>
                    </div>

                    <div className="text-center">
                        <button
                            type="button"
                            disabled={resendDisabled}
                            onClick={handleResendOtp}
                            className="text-sm text-blue-600 hover:text-blue-500 disabled:text-gray-400 disabled:cursor-not-allowed flex items-center justify-center mx-auto"
                        >
                            {resendDisabled ? (
                                <>
                                    <RefreshCw className="h-4 w-4 mr-1 animate-spin" />
                                    Resend code in {countdown}s
                                </>
                            ) : (
                                "Resend verification code"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}

